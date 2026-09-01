package com.payflow.notificationservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
public class Notification {

    @Id
    private String id;

    private Long userId;

    @Indexed(unique = true)
    private String txnId;

    private String type;
    private String message;
    private String status;
    private LocalDateTime createdAt;

    public Notification(Long userId, String txnId, String type,
                        String message, String status) {
        this.userId = userId;
        this.txnId = txnId;
        this.type = type;
        this.message = message;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}
