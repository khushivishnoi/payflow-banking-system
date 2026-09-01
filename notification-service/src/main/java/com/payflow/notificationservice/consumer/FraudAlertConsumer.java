package com.payflow.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.notificationservice.dto.FraudAlertEvent;
import com.payflow.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudAlertConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "fraud.alert",
                   groupId = "notification-fraud-group")
    public void onFraudAlert(ConsumerRecord<String, String> record) {
        log.info("Received fraud alert for notification: key={}", record.key());
        try {
            FraudAlertEvent event = objectMapper.readValue(
                    record.value(), FraudAlertEvent.class);

            String message = String.format(
                    "ALERT: Suspicious activity detected on your account. " +
                    "TxnId: %s, Amount: Rs %.2f, Reason: %s",
                    event.transactionId(), event.amount(), event.reason());

            notificationService.saveNotification(
                    event.fromAccountId(),
                    event.transactionId() + "-fraud",
                    "FRAUD_ALERT", message);

        } catch (Exception e) {
            log.error("Failed to process fraud alert for notification: {}",
                    e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
