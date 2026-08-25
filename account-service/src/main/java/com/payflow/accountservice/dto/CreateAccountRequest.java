package com.payflow.accountservice.dto;

import com.payflow.accountservice.entity.Account.AccountType;

public record CreateAccountRequest(Long userId, AccountType accountType) {}
