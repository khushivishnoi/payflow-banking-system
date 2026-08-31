package com.payflow.fraudservice.dto;

import java.math.BigDecimal;

public record FraudRulesDto(
    BigDecimal maxAmount,
    int velocityLimit,
    int velocityWindowSeconds,
    BigDecimal newPayeeThreshold
) {}
