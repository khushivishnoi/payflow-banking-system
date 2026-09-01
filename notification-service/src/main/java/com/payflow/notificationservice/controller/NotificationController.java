package com.payflow.notificationservice.controller;

import com.payflow.common.dto.ApiResponse;
import com.payflow.notificationservice.dto.NotificationDto;
import com.payflow.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getHistory(userId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationDto>> getById(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getById(id)));
    }
}
