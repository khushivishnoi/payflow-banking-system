package com.payflow.transactionservice.dto;

import java.math.BigDecimal;

public record BalanceUpdateRequest(BigDecimal amount, String operation) {}
