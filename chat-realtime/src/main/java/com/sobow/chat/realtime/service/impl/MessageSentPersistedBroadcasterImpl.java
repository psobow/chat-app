package com.sobow.chat.realtime.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.chat.common.domain.dto.MessageSentPersistedEventDto;
import com.sobow.chat.realtime.service.MessageSentPersistedBroadcaster;
import com.sobow.chat.realtime.service.MessageSentPersistedEventConsumer;
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
public class MessageSentPersistedBroadcasterImpl implements MessageSentPersistedBroadcaster {
    
    private final static String CHAT_MESSAGE_SENT = "chat:message-sent";
    
    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final MessageSentPersistedEventConsumer messageSentPersistedEventConsumer;
    
    private final Sinks.Many<MessageSentPersistedEventDto> sink = Sinks.many().multicast().onBackpressureBuffer();
    
    @PostConstruct
    public void listen() {
        // Kafka -> Redis
        messageSentPersistedEventConsumer
            .listen()
            .doOnSubscribe(subscription -> log.debug("Listening to message sent persisted events"))
            .flatMap(event -> redisTemplate.convertAndSend(CHAT_MESSAGE_SENT, event))
            .doOnNext(next -> log.debug("Stored object in Redis: {}", next))
            .onErrorContinue((error, object) -> log.error("Error storing object in Redis: ", error))
            .subscribe();
        
        // Redis -> Local Sinks
        redisTemplate
            .listenTo(ChannelTopic.of(CHAT_MESSAGE_SENT))
            .doOnSubscribe(subscription -> log.debug("Listening to objects put into redis"))
            .map(message -> objectMapper.convertValue(message.getMessage(), MessageSentPersistedEventDto.class))
            .doOnNext(next -> log.debug("Received object from redis: {}", next))
            .onErrorContinue((error, object) -> log.error("Error retrieving object from Redis: ", error))
            .subscribe(sink::tryEmitNext);
    }
    
    @Override
    public Flux<MessageSentPersistedEventDto> subscribe() {
        return sink.asFlux();
    }
}
