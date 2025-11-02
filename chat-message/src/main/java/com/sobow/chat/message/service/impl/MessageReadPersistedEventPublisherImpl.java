package com.sobow.chat.message.service.impl;

import static com.sobow.chat.common.KafkaTopics.KAFKA_TOPIC_MESSAGE_READ_PERSISTED;

import com.sobow.chat.common.domain.dto.MessageReadPersistedEventDto;
import com.sobow.chat.message.service.MessageReadPersistedEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MessageReadPersistedEventPublisherImpl implements MessageReadPersistedEventPublisher {
    
    private final ReactiveKafkaProducerTemplate<String, MessageReadPersistedEventDto> kafkaProducer;
    
    @Override
    public Mono<Void> publish(MessageReadPersistedEventDto event) {
        return kafkaProducer.send(
            KAFKA_TOPIC_MESSAGE_READ_PERSISTED,
            getPartitionKey(event),
            event
        ).then();
    }
    
    private String getPartitionKey(MessageReadPersistedEventDto event) {
        return event.getSenderId() + ":" + event.getReceiverId();
    }
}
