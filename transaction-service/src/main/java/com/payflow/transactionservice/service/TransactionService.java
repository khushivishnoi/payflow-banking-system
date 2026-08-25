package com.payflow.transactionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.exception.ResourceNotFoundException;
import com.payflow.transactionservice.client.AccountServiceClient;
import com.payflow.transactionservice.dto.*;
import com.payflow.transactionservice.entity.OutboxEvent;
import com.payflow.transactionservice.entity.Transaction;
import com.payflow.transactionservice.entity.Transaction.TransactionStatus;
import com.payflow.transactionservice.repository.OutboxEventRepository;
import com.payflow.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final AccountServiceClient accountServiceClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public TransactionDto transfer(String idempotencyKey, TransferRequest request) {
        // Idempotency check — same key returns cached result
        return transactionRepository.findByIdempotencyKey(idempotencyKey)
                .map(this::toDto)
                .orElseGet(() -> executeTransfer(idempotencyKey, request));
    }

    private TransactionDto executeTransfer(String idempotencyKey, TransferRequest request) {
        // Create transaction record
        Transaction txn = new Transaction();
        txn.setTransactionId(UUID.randomUUID().toString());
        txn.setFromAccountId(request.fromAccountId());
        txn.setToAccountId(request.toAccountId());
        txn.setAmount(request.amount());
        txn.setDescription(request.description());
        txn.setIdempotencyKey(idempotencyKey);
        txn.setStatus(TransactionStatus.PENDING);
        transactionRepository.save(txn);

        try {
            // Step 1: Debit from account
            accountServiceClient.updateBalance(request.fromAccountId(),
                    new BalanceUpdateRequest(request.amount(), "DEBIT"));

            // Step 2: Credit to account
            accountServiceClient.updateBalance(request.toAccountId(),
                    new BalanceUpdateRequest(request.amount(), "CREDIT"));

            // Step 3: Mark completed + write outbox event (SAME transaction)
            txn.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(txn);
            writeOutboxEvent(txn, "transaction.completed");

            log.info("Transfer completed: {}", txn.getTransactionId());

        } catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());

            // Saga compensation — try to reverse debit
            try {
                accountServiceClient.updateBalance(request.fromAccountId(),
                        new BalanceUpdateRequest(request.amount(), "CREDIT"));
                log.info("Compensating transaction applied for: {}", txn.getTransactionId());
            } catch (Exception ce) {
                log.error("Compensation also failed: {}", ce.getMessage());
            }

            txn.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(txn);
            writeOutboxEvent(txn, "transaction.failed");
        }

        return toDto(txn);
    }

    private void writeOutboxEvent(Transaction txn, String eventType) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "transactionId", txn.getTransactionId(),
                    "fromAccountId", txn.getFromAccountId(),
                    "toAccountId", txn.getToAccountId(),
                    "amount", txn.getAmount(),
                    "status", txn.getStatus().name()
            ));

            OutboxEvent event = new OutboxEvent();
            event.setAggregateType("Transaction");
            event.setAggregateId(txn.getTransactionId());
            event.setEventType(eventType);
            event.setPayload(payload);
            event.setPublished(false);
            outboxEventRepository.save(event);

        } catch (Exception e) {
            log.error("Failed to write outbox event", e);
        }
    }

    public TransactionDto getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
    }

    public Page<TransactionDto> getHistory(Long accountId, Pageable pageable) {
        return transactionRepository
                .findByFromAccountIdOrToAccountId(accountId, accountId, pageable)
                .map(this::toDto);
    }

    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(t.getId(), t.getTransactionId(),
                t.getFromAccountId(), t.getToAccountId(),
                t.getAmount(), t.getDescription(),
                t.getStatus(), t.getCreatedAt());
    }
}
