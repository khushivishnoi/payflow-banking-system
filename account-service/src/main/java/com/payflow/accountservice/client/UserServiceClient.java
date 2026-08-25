package com.payflow.accountservice.client;

import com.payflow.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {

    @GetMapping("/api/users/profile/{userId}")
    ApiResponse<?> getUserById(@PathVariable Long userId);
}
