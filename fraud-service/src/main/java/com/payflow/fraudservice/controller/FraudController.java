package com.payflow.fraudservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.fraudservice.dto.FraudRulesDto;
import com.payflow.fraudservice.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<FraudRulesDto>> getRules() {
        return ResponseEntity.ok(ApiResponse.ok(fraudDetectionService.getRules()));
    }

    @PostMapping("/rules")
    public ResponseEntity<ApiResponse<String>> updateRules(@RequestBody FraudRulesDto rules) {
        fraudDetectionService.updateRules(rules);
        return ResponseEntity.ok(ApiResponse.ok("Rules updated successfully"));
    }

    @GetMapping("/flags/{txnId}")
    public ResponseEntity<ApiResponse<String>> checkFlag(@PathVariable String txnId) {
        return ResponseEntity.ok(ApiResponse.ok(fraudDetectionService.checkFlag(txnId)));
    }
}
