package com.sobow.chat.realtime.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.chat.common.domain.dto.MessageSentEventDto;
import com.sobow.chat.realtime.domain.event.WebSocketEventEnvelope;
import com.sobow.chat.realtime.domain.event.WebSocketEventType;
import com.sobow.chat.realtime.exception.WebSocketUnauthorizedException;
import com.sobow.chat.realtime.service.MessageSentEventPublisher;
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
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketConnectionHandler implements WebSocketHandler {
    
    private final ReactiveJwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;
    private final MessageSentEventPublisher messageSentEventPublisher;
    
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
        return sendConnectionAcknowledgedMessage(session)
            .then(handleIncomingWebSocketMessage(session));
    }
    
    private Mono<Void> sendConnectionAcknowledgedMessage(WebSocketSession session) {
        return serializeWebSocketConnectionAckEvent()
            .map(session::textMessage)
            .as(session::send)
            .then();
    }
    
    private Mono<Void> handleIncomingWebSocketMessage(WebSocketSession session) {
        return session
            .receive()
            .map(WebSocketMessage::getPayloadAsText)
            .doOnNext(next -> log.debug("Received WebSocket message {}", next))
            .flatMap(this::parseWebSocketEventEnvelope)
            .flatMap(event ->
                         switch (event.getType()) {
                             case MESSAGE_SENT -> handleIncomingMessageSentEvent(event);
                             default -> handleUnsupportedEvent(event);
                         })
            .onErrorResume(error -> {
                log.error("Error occurred while handling incoming WebSocket message: ", error);
                return Mono.empty();
            }).then();
    }
    
    private Mono<WebSocketEventEnvelope<?>> parseWebSocketEventEnvelope(String rawMessage) {
        return Mono.defer(() -> {
            try {
                var envelope = objectMapper.readValue(rawMessage, new TypeReference<WebSocketEventEnvelope<?>>() {
                });
                return Mono.just(envelope);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse WS envelope: {}", rawMessage, e);
                return Mono.empty();
            }
        });
    }
    
    private Mono<Void> handleIncomingMessageSentEvent(WebSocketEventEnvelope<?> event) {
        return parseMessageSentEvent(event)
            .doOnNext(next -> log.debug("Received WebSocket message sent event {}", next))
            .flatMap(messageSentEventPublisher::publish)
            .doOnSuccess(onSuccess -> log.debug("WebSocket message sent event successfully published to Kafka: {}", event))
            .doOnError(onError -> log.error("WebSocket message sent event failed to publish to Kafka: {}", event));
    }
    
    private Mono<MessageSentEventDto> parseMessageSentEvent(WebSocketEventEnvelope<?> event) {
        return parseEvent(event, MessageSentEventDto.class);
    }
    
    private <T> Mono<T> parseEvent(WebSocketEventEnvelope<?> envelope, Class<T> payloadClass) {
        return Mono.defer(() -> {
            try {
                T value = objectMapper.convertValue(envelope.getPayload(), payloadClass);
                return Mono.just(value);
            } catch (IllegalArgumentException e) {
                return Mono.error(e);
            }
        });
    }
    
    private Mono<Void> handleUnsupportedEvent(WebSocketEventEnvelope<?> unsupportedEvent) {
        log.debug("Received unsupported event {}", unsupportedEvent);
        return Mono.empty();
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
