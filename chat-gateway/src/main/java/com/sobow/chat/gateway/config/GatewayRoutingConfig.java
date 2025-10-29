package com.sobow.chat.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutingConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder
            .routes()
            // Contact Service
            .route("contacts",
                   r -> r.path("/api/v1/contacts/**")
                         .uri("http://localhost:8081")
            )
            // Real Time Communication Service
            .route("websocket",
                   r -> r.path("/api/v1/ws/**")
                         .and()
                         .header("Upgrade", "websocket")
                         .uri("http://localhost:8082")
            )
            // Message Service
            .route("message",
                   r -> r.path("/api/v1/messages/**")
                         .uri("http://localhost:8083")
            )
            .build();
    }
}
