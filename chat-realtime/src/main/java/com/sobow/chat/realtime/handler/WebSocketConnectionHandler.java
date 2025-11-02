package com.sobow.chat.realtime.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.chat.common.domain.dto.MessageReadEventDto;
import com.sobow.chat.common.domain.dto.MessageReadPersistedEventDto;
import com.sobow.chat.common.domain.dto.MessageSentEventDto;
import com.sobow.chat.common.domain.dto.MessageSentPersistedEventDto;
import com.sobow.chat.realtime.domain.event.WebSocketEventEnvelope;
import com.sobow.chat.realtime.domain.event.WebSocketEventType;
import com.sobow.chat.realtime.exception.WebSocketUnauthorizedException;
import com.sobow.chat.realtime.service.MessageReadEventPublisher;
import com.sobow.chat.realtime.service.MessageReadPersistedBroadcaster;
import com.sobow.chat.realtime.service.MessageSentEventPublisher;
import com.sobow.chat.realtime.service.MessageSentPersistedBroadcaster;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketConnectionHandler implements WebSocketHandler {
    
    private final ReactiveJwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;
    
    private final MessageSentEventPublisher messageSentEventPublisher;
    private final MessageReadEventPublisher messageReadEventPublisher;
    
    private final MessageSentPersistedBroadcaster messageSentPersistedBroadcaster;
    private final MessageReadPersistedBroadcaster messageReadPersistedBroadcaster;
    
    
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
            .then(Mono.when(
                handleIncomingWebSocketMessage(session),
                handleOutgoingWebSocketMessage(session, userId)
            ));
    }
    
    private Mono<Void> sendConnectionAcknowledgedMessage(WebSocketSession session) {
        return Mono.just(buildConnectionAckEnvelope())
                   .flatMap(this::serializeWebSocketEvent)
                   .map(session::textMessage)
                   .as(session::send)
                   .then();
    }
    
    private Mono<Void> handleOutgoingWebSocketMessage(WebSocketSession session, UUID userId) {
        Flux<String> messageSentPersistedEvents = messageSentPersistedBroadcaster
            .subscribe()
            .filter(event -> userId.equals(event.getSenderId()) || userId.equals(event.getReceiverId()))
            .flatMap(this::serializeMessageSentPersistedEvent);
        
        Flux<String> messageReadPersistedEvents = messageReadPersistedBroadcaster
            .subscribe()
            .filter(event -> userId.equals(event.getSenderId()))
            .flatMap(this::serializeMessageReadPersistedEvent);
        
        return Flux.merge(messageSentPersistedEvents, messageReadPersistedEvents)
            .doOnNext(next -> log.debug("WebSocket message out: {}", next))
            .map(session::textMessage)
            .as(session::send)
            .onErrorResume(error -> {
                log.error("Error sending WebSocket message:", error);
                return Mono.empty();
            });
    }
    
    private Mono<String> serializeMessageSentPersistedEvent(MessageSentPersistedEventDto event) {
        WebSocketEventEnvelope<MessageSentPersistedEventDto> envelope =
            WebSocketEventEnvelope.<MessageSentPersistedEventDto>builder()
                                  .id(UUID.randomUUID())
                                  .type(WebSocketEventType.MESSAGE_SENT)
                                  .payload(event)
                                  .timestamp(Instant.now())
                                  .build();
        return serializeWebSocketEvent(envelope);
    }
    
    private Mono<String> serializeMessageReadPersistedEvent(MessageReadPersistedEventDto event) {
        WebSocketEventEnvelope<MessageReadPersistedEventDto> envelope =
            WebSocketEventEnvelope.<MessageReadPersistedEventDto>builder()
                                  .id(UUID.randomUUID())
                                  .type(WebSocketEventType.MESSAGE_READ)
                                  .payload(event)
                                  .timestamp(Instant.now())
                                  .build();
        return serializeWebSocketEvent(envelope);
    }
    
    private Mono<Void> handleIncomingWebSocketMessage(WebSocketSession session) {
        return session
            .receive()
            .map(WebSocketMessage::getPayloadAsText)
            .doOnNext(next -> log.debug("Received WebSocket message {}", next))
            .flatMap(this::deserializeEventEnvelope)
            .flatMap(envelope ->
                         switch (envelope.getType()) {
                             case MESSAGE_SENT -> handleMessageSentEvent(envelope);
                             case MESSAGE_READ -> handleMessageReadEvent(envelope);
                             default -> handleUnsupportedEvent(envelope);
                         })
            .onErrorResume(error -> {
                log.error("Error occurred while handling incoming WebSocket message: ", error);
                return Mono.empty();
            }).then();
    }
    
    private Mono<WebSocketEventEnvelope<?>> deserializeEventEnvelope(String rawMessage) {
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
    
    private Mono<Void> handleMessageSentEvent(WebSocketEventEnvelope<?> envelope) {
        return extractMessageSentEvent(envelope)
            .doOnNext(next -> log.debug("Received WebSocket message sent event {}", next))
            .flatMap(messageSentEventPublisher::publish)
            .doOnSuccess(onSuccess -> log.debug("WebSocket message sent event successfully published to Kafka: {}", envelope))
            .doOnError(onError -> log.error("WebSocket message sent event failed to publish to Kafka: {}", envelope));
    }
    
    private Mono<Void> handleMessageReadEvent(WebSocketEventEnvelope<?> envelope) {
        return extractMessageReadEvent(envelope)
            .doOnNext(next -> log.debug("Received WebSocket message read event: {}", envelope))
            .flatMap(messageReadEventPublisher::publish)
            .then();
    }
    
    private Mono<MessageSentEventDto> extractMessageSentEvent(WebSocketEventEnvelope<?> envelope) {
        if (envelope.getType() != WebSocketEventType.MESSAGE_SENT) {
            return Mono.error(new IllegalArgumentException("Unexpected event type: " + envelope.getType()));
        }
        
        return extractPayload(envelope, MessageSentEventDto.class);
    }
    
    private Mono<MessageReadEventDto> extractMessageReadEvent(WebSocketEventEnvelope<?> envelope) {
        if (envelope.getType() != WebSocketEventType.MESSAGE_READ) {
            return Mono.error(new IllegalArgumentException("Unexpected event type: " + envelope.getType()));
        }
        return extractPayload(envelope, MessageReadEventDto.class);
    }
    
    private <T> Mono<T> extractPayload(WebSocketEventEnvelope<?> envelope, Class<T> payloadClass) {
        return Mono.defer(() -> {
            try {
                T value = objectMapper.convertValue(envelope.getPayload(), payloadClass);
                return Mono.just(value);
            } catch (IllegalArgumentException e) {
                return Mono.error(e);
            }
        });
    }
    
    private Mono<Void> handleUnsupportedEvent(WebSocketEventEnvelope<?> unsupportedEnvelope) {
        log.debug("Received unsupported event {}", unsupportedEnvelope);
        return Mono.empty();
    }
    
    private WebSocketEventEnvelope<String> buildConnectionAckEnvelope() {
        WebSocketEventEnvelope<String> ackMessage = WebSocketEventEnvelope.<String>builder()
                                                                          .id(UUID.randomUUID())
                                                                          .type(WebSocketEventType.CONNECTION_ACK)
                                                                          .payload("Connection acknowledged")
                                                                          .timestamp(Instant.now())
                                                                          .build();
        return ackMessage;
    }
    
    private Mono<String> serializeWebSocketEvent(WebSocketEventEnvelope<?> envelope) {
        try {
            return Mono.fromCallable(() -> objectMapper.writeValueAsString(envelope));
        } catch (Exception e) {
            log.error("Error serializing WebSocket envelope", e);
            return Mono.error(e);
        }
    }
}
