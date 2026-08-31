package com.payflow.fraudservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.fraudservice.dto.TransactionEvent;
import com.payflow.fraudservice.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final FraudDetectionService fraudDetectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "transaction.completed", groupId = "fraud-service-group")
    public void onTransactionCompleted(ConsumerRecord<String, String> record) {
        log.info("Received transaction event: key={}", record.key());
        try {
            TransactionEvent event = objectMapper.readValue(record.value(), TransactionEvent.class);
            fraudDetectionService.analyze(event);
        } catch (Exception e) {
            log.error("Failed to process transaction event: {}", e.getMessage(), e);
        }
    }
}
