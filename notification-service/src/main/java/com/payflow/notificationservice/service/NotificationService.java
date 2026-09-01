package com.payflow.notificationservice.service;

import com.payflow.notificationservice.dto.NotificationDto;
import com.payflow.notificationservice.entity.Notification;
import com.payflow.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void saveNotification(Long userId, String txnId,
                                  String type, String message) {
        // Idempotency — skip if already processed
        if (notificationRepository.existsByTxnId(txnId)) {
            log.info("Notification already processed for txnId: {}", txnId);
            return;
        }

        Notification notification = new Notification(
                userId, txnId, type, message, "SENT"
        );
        notificationRepository.save(notification);
        log.info("Notification saved: type={}, txnId={}", type, txnId);

        // Mock send — in production plug in SMTP/SMS here
        log.info("[MOCK {}] To userId={}: {}", type, userId, message);
    }

    public Page<NotificationDto> getHistory(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
                .map(this::toDto);
    }

    public NotificationDto getById(String id) {
        return notificationRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
    }

    private NotificationDto toDto(Notification n) {
        return new NotificationDto(n.getId(), n.getUserId(), n.getTxnId(),
                n.getType(), n.getMessage(), n.getStatus(), n.getCreatedAt());
    }
}
