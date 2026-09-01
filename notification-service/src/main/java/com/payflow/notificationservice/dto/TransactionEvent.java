package com.payflow.notificationservice.dto;

import java.math.BigDecimal;

public record TransactionEvent(
    String transactionId,
    Long fromAccountId,
    Long toAccountId,
    BigDecimal amount,
    String status
) {}
