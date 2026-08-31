package com.payflow.fraudservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.fraudservice.dto.FraudAlertEvent;
import com.payflow.fraudservice.dto.FraudRulesDto;
import com.payflow.fraudservice.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${fraud.rule.max-amount}")
    private BigDecimal maxAmount;

    @Value("${fraud.rule.velocity-limit}")
    private int velocityLimit;

    @Value("${fraud.rule.velocity-window-seconds}")
    private int velocityWindowSeconds;

    @Value("${fraud.rule.new-payee-threshold}")
    private BigDecimal newPayeeThreshold;

    // Redis key prefixes
    private static final String VELOCITY_KEY = "fraud:velocity:";
    private static final String PAYEE_KEY = "fraud:payee:";
    private static final String FLAG_KEY = "fraud:flag:";
    private static final String RULES_KEY = "fraud:rules";

    public void analyze(TransactionEvent event) {
        List<String> triggeredReasons = new ArrayList<>();

        // Rule 1: Large amount
        BigDecimal ruleMaxAmount = getRuleMaxAmount();
        if (event.amount().compareTo(ruleMaxAmount) > 0) {
            triggeredReasons.add("LARGE_AMOUNT: " + event.amount() + " exceeds limit " + ruleMaxAmount);
        }

        // Rule 2: Velocity check — too many txns in short time
        String velocityKey = VELOCITY_KEY + event.fromAccountId();
        Long count = redisTemplate.opsForValue().increment(velocityKey);
        if (count == 1) {
            redisTemplate.expire(velocityKey, Duration.ofSeconds(velocityWindowSeconds));
        }
        if (count != null && count > getRuleVelocityLimit()) {
            triggeredReasons.add("VELOCITY: " + count + " transactions in " + velocityWindowSeconds + "s");
        }

        // Rule 3: New payee + large amount
        String payeeKey = PAYEE_KEY + event.fromAccountId() + ":" + event.toAccountId();
        Boolean isNewPayee = redisTemplate.opsForValue().setIfAbsent(payeeKey, "seen");
        if (Boolean.TRUE.equals(isNewPayee)) {
            // Set expiry of 30 days for payee tracking
            redisTemplate.expire(payeeKey, Duration.ofDays(30));
            if (event.amount().compareTo(getRuleNewPayeeThreshold()) > 0) {
                triggeredReasons.add("NEW_PAYEE: First transaction to account "
                        + event.toAccountId() + " with amount " + event.amount());
            }
        }

        if (!triggeredReasons.isEmpty()) {
            String reason = String.join("; ", triggeredReasons);
            log.warn("FRAUD DETECTED for txn {}: {}", event.transactionId(), reason);
            flagTransaction(event, reason);
            publishFraudAlert(event, reason);
        } else {
            log.info("Transaction {} passed fraud checks", event.transactionId());
        }
    }

    private void flagTransaction(TransactionEvent event, String reason) {
        try {
            String flagKey = FLAG_KEY + event.transactionId();
            redisTemplate.opsForValue().set(flagKey, reason);
            redisTemplate.expire(flagKey, Duration.ofDays(90));
        } catch (Exception e) {
            log.error("Failed to flag transaction in Redis", e);
        }
    }

    private void publishFraudAlert(TransactionEvent event, String reason) {
        try {
            FraudAlertEvent alert = new FraudAlertEvent(
                    event.transactionId(),
                    event.fromAccountId(),
                    event.toAccountId(),
                    event.amount(),
                    reason,
                    LocalDateTime.now()
            );
            String payload = objectMapper.writeValueAsString(alert);
            kafkaTemplate.send("fraud.alert", event.transactionId(), payload);
            log.info("Fraud alert published for txn: {}", event.transactionId());
        } catch (Exception e) {
            log.error("Failed to publish fraud alert", e);
        }
    }

    public String checkFlag(String transactionId) {
        String flagKey = FLAG_KEY + transactionId;
        String flag = redisTemplate.opsForValue().get(flagKey);
        return flag != null ? flag : "NOT_FLAGGED";
    }

    public FraudRulesDto getRules() {
        return new FraudRulesDto(
                getRuleMaxAmount(),
                getRuleVelocityLimit(),
                velocityWindowSeconds,
                getRuleNewPayeeThreshold()
        );
    }

    public void updateRules(FraudRulesDto rules) {
        redisTemplate.opsForHash().put(RULES_KEY, "maxAmount", rules.maxAmount().toString());
        redisTemplate.opsForHash().put(RULES_KEY, "velocityLimit", String.valueOf(rules.velocityLimit()));
        redisTemplate.opsForHash().put(RULES_KEY, "newPayeeThreshold", rules.newPayeeThreshold().toString());
        log.info("Fraud rules updated: {}", rules);
    }

    // Rule getters — check Redis first, fall back to config
    private BigDecimal getRuleMaxAmount() {
        String val = (String) redisTemplate.opsForHash().get(RULES_KEY, "maxAmount");
        return val != null ? new BigDecimal(val) : maxAmount;
    }

    private int getRuleVelocityLimit() {
        String val = (String) redisTemplate.opsForHash().get(RULES_KEY, "velocityLimit");
        return val != null ? Integer.parseInt(val) : velocityLimit;
    }

    private BigDecimal getRuleNewPayeeThreshold() {
        String val = (String) redisTemplate.opsForHash().get(RULES_KEY, "newPayeeThreshold");
        return val != null ? new BigDecimal(val) : newPayeeThreshold;
    }
}
