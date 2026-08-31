package com.payflow.fraudservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FraudAlertEvent(
    String transactionId,
    Long fromAccountId,
    Long toAccountId,
    BigDecimal amount,
    String reason,
    LocalDateTime detectedAt
) {}
