package com.payflow.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.notificationservice.dto.TransactionEvent;
import com.payflow.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "transaction.completed",
                   groupId = "notification-service-group")
    public void onTransactionCompleted(ConsumerRecord<String, String> record) {
        log.info("Received transaction event for notification: key={}", record.key());
        try {
            TransactionEvent event = objectMapper.readValue(
                    record.value(), TransactionEvent.class);

            // Notify sender
            String debitMsg = String.format(
                    "Rs %.2f debited from your account. TxnId: %s",
                    event.amount(), event.transactionId());
            notificationService.saveNotification(
                    event.fromAccountId(), event.transactionId() + "-debit",
                    "DEBIT_ALERT", debitMsg);

            // Notify receiver
            String creditMsg = String.format(
                    "Rs %.2f credited to your account. TxnId: %s",
                    event.amount(), event.transactionId());
            notificationService.saveNotification(
                    event.toAccountId(), event.transactionId() + "-credit",
                    "CREDIT_ALERT", creditMsg);

        } catch (Exception e) {
            log.error("Failed to process transaction event for notification: {}",
                    e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
