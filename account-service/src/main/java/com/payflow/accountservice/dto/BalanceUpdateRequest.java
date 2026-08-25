package com.payflow.accountservice.dto;

import java.math.BigDecimal;

public record BalanceUpdateRequest(BigDecimal amount, String operation) {}
