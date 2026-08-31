package com.payflow.fraudservice.dto;

import java.math.BigDecimal;

public record TransactionEvent(
    String transactionId,
    Long fromAccountId,
    Long toAccountId,
    BigDecimal amount,
    String status
) {}
