package com.sobow.chat.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtUserIdHeaderFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange
            .getPrincipal()
            .cast(JwtAuthenticationToken.class)
            .map(jwtToken -> {
                     String userId = jwtToken.getToken().getSubject();
                     ServerHttpRequest modifiedRequest = exchange.getRequest()
                                                                 .mutate()
                                                                 .header("X-User-Id", userId)
                                                                 .build();
                     return exchange.mutate()
                                    .request(modifiedRequest)
                                    .build();
                 }
            )
            .defaultIfEmpty(exchange)
            .flatMap(chain::filter);
    }
    
    @Override
    public int getOrder() {
        // Run before routing
        return -100;
    }
}
