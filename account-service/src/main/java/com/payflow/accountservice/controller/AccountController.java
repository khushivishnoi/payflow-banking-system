package com.payflow.accountservice.controller;

import com.payflow.accountservice.dto.*;
import com.payflow.accountservice.service.AccountService;
import com.payflow.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(@RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(accountService.createAccount(request)));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<AccountDto>> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAccountById(id)));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getBalance(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AccountDto>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAccountsByUserId(userId)));
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<AccountDto>> updateBalance(
            @PathVariable Long id,
            @RequestBody BalanceUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.updateBalance(id, request)));
    }
}
