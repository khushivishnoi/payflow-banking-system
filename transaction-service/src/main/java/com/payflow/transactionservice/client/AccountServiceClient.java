package com.payflow.transactionservice.client;

import com.payflow.common.dto.ApiResponse;
import com.payflow.transactionservice.dto.BalanceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-service", url = "${account-service.url}")
public interface AccountServiceClient {

    @PutMapping("/api/accounts/{id}/balance")
    ApiResponse<?> updateBalance(@PathVariable Long id,
                                  @RequestBody BalanceUpdateRequest request);

    @GetMapping("/api/accounts/{id}/balance")
    ApiResponse<?> getBalance(@PathVariable Long id);
}
