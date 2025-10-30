package com.sobow.chat.realtime.service.impl;

import com.sobow.chat.common.domain.dto.MessageSentPersistedEventDto;
import com.sobow.chat.realtime.service.MessageSentPersistedEventConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class MessageSentPersistedEventConsumerImpl implements MessageSentPersistedEventConsumer {
    
    private final ReactiveKafkaConsumerTemplate<String, MessageSentPersistedEventDto> kafkaConsumer;
    
    @Override
    public Flux<MessageSentPersistedEventDto> listen() {
        return kafkaConsumer
            .receiveAutoAck()
            .map(ConsumerRecord::value);
    }
}
