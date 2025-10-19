package com.sobow.chat.realtime.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.chat.realtime.domain.event.WebSocketEventEnvelope;
import com.sobow.chat.realtime.domain.event.WebSocketEventType;
import com.sobow.chat.realtime.exception.WebSocketUnauthorizedException;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketConnectionHandler implements WebSocketHandler {
    
    private final ReactiveJwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return authenticateWebSocketConnection(session)
            .switchIfEmpty(Mono.error(new WebSocketUnauthorizedException()))
            .flatMap(userId -> handleAuthenticatedWebSocketConnection(session, userId))
            .onErrorResume(e -> {
                log.error("Error occurred while handling WebSocket connection", e);
                return session.close(CloseStatus.NOT_ACCEPTABLE);
            });
    }
    
    private Mono<UUID> authenticateWebSocketConnection(WebSocketSession session) {
        try {
            URI uri = session.getHandshakeInfo().getUri();
            MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
            String token = queryParams.getFirst("token");
            if (token == null || token.isEmpty()) {
                return Mono.empty();
            }
            
            return jwtDecoder.decode(token)
                             .map(JwtClaimAccessor::getSubject)
                             .map(UUID::fromString)
                             .onErrorResume(ex -> {
                                 log.error("Error decoding JWT {}", token);
                                 return Mono.empty();
                             });
        } catch (Exception e) {
            log.error("Invalid JWT passed to authenticate WebSocket", e);
            return Mono.empty();
        }
    }
    
    private Mono<Void> handleAuthenticatedWebSocketConnection(
        WebSocketSession session,
        UUID userId
    ) {
        return sendConnectionAcknowledgedMessage(session);
    }
    
    private Mono<Void> sendConnectionAcknowledgedMessage(WebSocketSession session) {
        return serializeWebSocketConnectionAckEvent()
            .map(session::textMessage)
            .as(session::send)
            .then();
    }
    
    private Mono<String> serializeWebSocketConnectionAckEvent() {
        WebSocketEventEnvelope<String> ackMessage = WebSocketEventEnvelope.<String>builder()
                                                                          .id(UUID.randomUUID())
                                                                          .type(WebSocketEventType.CONNECTION_ACK)
                                                                          .payload("Connection acknowledge")
                                                                          .timestamp(Instant.now())
                                                                          .build();
        return serializeWebSocketEvent(ackMessage);
    }
    
    private Mono<String> serializeWebSocketEvent(WebSocketEventEnvelope<?> event) {
        try {
            return Mono.fromCallable(() -> objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Error serializing WebSocket event", e);
            return Mono.error(e);
        }
    }
}
