package com.payflow.account_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.payflow.account_service", "com.payflow.accountservice"})
@EnableJpaRepositories(basePackages = "com.payflow.accountservice.repository")
@EntityScan(basePackages = "com.payflow.accountservice.entity")
@EnableFeignClients(basePackages = "com.payflow.accountservice.client")
public class AccountServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
