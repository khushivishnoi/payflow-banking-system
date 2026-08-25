package com.payflow.transactionservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.transactionservice.dto.*;
import com.payflow.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionDto>> transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody TransferRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.transfer(idempotencyKey, request)));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionDto>> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getTransaction(transactionId)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<TransactionDto>>> getHistory(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getHistory(accountId, pageable)));
    }
}
