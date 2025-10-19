package com.sobow.chat.realtime.config;

import com.sobow.chat.realtime.handler.WebSocketConnectionHandler;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

@Configuration
public class WebSocketConfig {
    
    @Bean
    public HandlerMapping webSocketHandlerMapping(WebSocketConnectionHandler handler) {
        Map<String, WebSocketHandler> map = Map.of("/api/v1/ws", handler);
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(1);
        mapping.setUrlMap(map);
        return mapping;
    }
}
