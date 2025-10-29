package com.sobow.chat.message.service.impl;

import static com.sobow.chat.common.KafkaTopics.KAFKA_TOPIC_MESSAGE_SENT_PERSISTED;

import com.sobow.chat.common.domain.dto.MessageSentPersistedEventDto;
import com.sobow.chat.message.service.MessageSentPersistedEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MessageSentPersistedEventPublisherImpl implements MessageSentPersistedEventPublisher {
    
    private final ReactiveKafkaProducerTemplate<String, MessageSentPersistedEventDto> kafkaProducer;
    
    @Override
    public Mono<Void> publish(MessageSentPersistedEventDto event) {
        return kafkaProducer.send(
            KAFKA_TOPIC_MESSAGE_SENT_PERSISTED,
            getPartitionKey(event),
            event
        ).then();
    }
    
    private String getPartitionKey(MessageSentPersistedEventDto event) {
        return event.getSenderId() + ":" + event.getReceiverId();
    }
}
