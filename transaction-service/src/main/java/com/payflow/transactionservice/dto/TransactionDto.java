package com.payflow.transactionservice.dto;

import com.payflow.transactionservice.entity.Transaction.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDto(
    Long id,
    String transactionId,
    Long fromAccountId,
    Long toAccountId,
    BigDecimal amount,
    String description,
    TransactionStatus status,
    LocalDateTime createdAt
) {}
