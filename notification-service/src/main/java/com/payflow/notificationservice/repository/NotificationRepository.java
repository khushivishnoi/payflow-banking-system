package com.payflow.notificationservice.repository;

import com.payflow.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    Optional<Notification> findByTxnId(String txnId);
    boolean existsByTxnId(String txnId);
}
