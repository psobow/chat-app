package com.sobow.chat.realtime.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.chat.common.domain.dto.MessageReadPersistedEventDto;
import com.sobow.chat.realtime.service.MessageReadPersistedBroadcaster;
import com.sobow.chat.realtime.service.MessageReadPersistedEventConsumer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageReadPersistedBroadcasterImpl implements MessageReadPersistedBroadcaster {
    
    private static final String CHAT_MESSAGE_READ = "chat:message-read";
    
    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final MessageReadPersistedEventConsumer messageReadPersistedEventConsumer;
    
    private Sinks.Many<MessageReadPersistedEventDto> messageReadPersistedSink = Sinks.many().multicast().onBackpressureBuffer();
    
    @PostConstruct
    public void listen() {
        // Kafka -> Redis
        messageReadPersistedEventConsumer
            .listen()
            .doOnSubscribe(subscription -> log.debug("Listening to message read persisted events from Kafka..."))
            .flatMap(event -> redisTemplate.convertAndSend(CHAT_MESSAGE_READ, event))
            .doOnNext(next -> log.debug("Pushing event to Redis: {}", next))
            .doOnError(error -> log.error("Error saving message in Redis", error))
            .subscribe();
        
        // Redis -> Local Sinks
        redisTemplate
            .listenTo(ChannelTopic.of(CHAT_MESSAGE_READ))
            .doOnSubscribe(subscription -> log.debug("Listening to message read persisted events from Redis..."))
            .map(message -> objectMapper.convertValue(message.getMessage(), MessageReadPersistedEventDto.class))
            .doOnNext(next -> log.debug("Received event from Redis: {}", next))
            .doOnError(error -> log.error("Error reading message from Redis", error))
            .subscribe(messageReadPersistedSink::tryEmitNext);
    }
    
    @Override
    public Flux<MessageReadPersistedEventDto> subscribe() {
        return messageReadPersistedSink.asFlux();
    }
}
