package com.payflow.notificationservice.dto;

import java.time.LocalDateTime;

public record NotificationDto(
    String id,
    Long userId,
    String txnId,
    String type,
    String message,
    String status,
    LocalDateTime createdAt
) {}
