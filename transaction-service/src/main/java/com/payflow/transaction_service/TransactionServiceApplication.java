package com.payflow.transaction_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.payflow.transaction_service", "com.payflow.transactionservice"})
@EnableJpaRepositories(basePackages = "com.payflow.transactionservice.repository")
@EntityScan(basePackages = "com.payflow.transactionservice.entity")
@EnableFeignClients(basePackages = "com.payflow.transactionservice.client")
@EnableScheduling
public class TransactionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}
