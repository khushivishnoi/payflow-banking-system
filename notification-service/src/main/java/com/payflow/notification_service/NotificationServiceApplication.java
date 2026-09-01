package com.payflow.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {
    "com.payflow.notification_service",
    "com.payflow.notificationservice"
})
@EnableMongoRepositories(basePackages = "com.payflow.notificationservice.repository")
@EnableKafka
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
