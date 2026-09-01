package com.payflow.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Value("${account-service.url}")
    private String accountServiceUrl;

    @Value("${transaction-service.url}")
    private String transactionServiceUrl;

    @Value("${notification-service.url}")
    private String notificationServiceUrl;

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // replenishRate=3 per sec, burstCapacity=5, requestedTokens=1
        return new RedisRateLimiter(3, 5, 1);
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder,
                                      RedisRateLimiter redisRateLimiter,
                                      KeyResolver userKeyResolver) {
        return builder.routes()

                .route("user-service-public", r -> r
                        .path("/api/users/register", "/api/users/login", "/api/users/refresh")
                        .uri(userServiceUrl))

                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.requestRateLimiter(c -> {
                            c.setRateLimiter(redisRateLimiter);
                            c.setKeyResolver(userKeyResolver);
                            c.setDenyEmptyKey(false);
                        }))
                        .uri(userServiceUrl))

                .route("account-service", r -> r
                        .path("/api/accounts/**")
                        .filters(f -> f.requestRateLimiter(c -> {
                            c.setRateLimiter(redisRateLimiter);
                            c.setKeyResolver(userKeyResolver);
                            c.setDenyEmptyKey(false);
                        }))
                        .uri(accountServiceUrl))

                .route("transaction-service", r -> r
                        .path("/api/transactions/**")
                        .filters(f -> f.requestRateLimiter(c -> {
                            c.setRateLimiter(redisRateLimiter);
                            c.setKeyResolver(userKeyResolver);
                            c.setDenyEmptyKey(false);
                        }))
                        .uri(transactionServiceUrl))

                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.requestRateLimiter(c -> {
                            c.setRateLimiter(redisRateLimiter);
                            c.setKeyResolver(userKeyResolver);
                            c.setDenyEmptyKey(false);
                        }))
                        .uri(notificationServiceUrl))

                .build();
    }
}
