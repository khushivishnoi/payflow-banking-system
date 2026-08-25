package com.payflow.accountservice.service;

import com.payflow.accountservice.client.UserServiceClient;
import com.payflow.accountservice.dto.*;
import com.payflow.accountservice.entity.Account;
import com.payflow.accountservice.repository.AccountRepository;
import com.payflow.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public AccountDto createAccount(CreateAccountRequest request) {
        // Validate user exists via Feign
        try {
            userServiceClient.getUserById(request.userId());
        } catch (Exception e) {
            throw new IllegalArgumentException("User not found with id: " + request.userId());
        }

        Account account = new Account();
        account.setUserId(request.userId());
        account.setAccountType(request.accountType());
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);

        return toDto(accountRepository.save(account));
    }

    public AccountDto getAccountById(Long id) {
        return toDto(findById(id));
    }

    public BigDecimal getBalance(Long id) {
        return findById(id).getBalance();
    }

    public List<AccountDto> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public AccountDto updateBalance(Long id, BalanceUpdateRequest request) {
        Account account = findById(id);

        if ("DEBIT".equals(request.operation())) {
            if (account.getBalance().compareTo(request.amount()) < 0)
                throw new IllegalArgumentException("Insufficient funds");
            account.setBalance(account.getBalance().subtract(request.amount()));
        } else {
            account.setBalance(account.getBalance().add(request.amount()));
        }

        return toDto(accountRepository.save(account));
    }

    private Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "PAY" + String.format("%010d", new Random().nextLong(9_999_999_999L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    private AccountDto toDto(Account a) {
        return new AccountDto(a.getId(), a.getUserId(), a.getAccountNumber(),
                a.getAccountType(), a.getBalance(), a.getCreatedAt());
    }
}
