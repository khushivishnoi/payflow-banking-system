package com.payflow.accountservice.dto;

import com.payflow.accountservice.entity.Account.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountDto(
    Long id,
    Long userId,
    String accountNumber,
    AccountType accountType,
    BigDecimal balance,
    LocalDateTime createdAt
) {}
