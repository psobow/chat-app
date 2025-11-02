package com.sobow.chat.realtime.service.impl;

import com.sobow.chat.common.domain.dto.MessageReadPersistedEventDto;
import com.sobow.chat.realtime.service.MessageReadPersistedEventConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class MessageReadPersistedEventConsumerImpl implements MessageReadPersistedEventConsumer {
    
    private final ReactiveKafkaConsumerTemplate<String, MessageReadPersistedEventDto> kafkaConsumer;
    
    @Override
    public Flux<MessageReadPersistedEventDto> listen() {
        return kafkaConsumer
            .receiveAutoAck()
            .map(ConsumerRecord::value);
    }
}
