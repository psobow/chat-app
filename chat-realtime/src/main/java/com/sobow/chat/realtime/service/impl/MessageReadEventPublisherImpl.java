package com.sobow.chat.realtime.service.impl;

import static com.sobow.chat.common.KafkaTopics.KAFKA_TOPIC_MESSAGE_READ;

import com.sobow.chat.common.domain.dto.MessageReadEventDto;
import com.sobow.chat.realtime.service.MessageReadEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MessageReadEventPublisherImpl implements MessageReadEventPublisher {
    
    private final ReactiveKafkaProducerTemplate<String, MessageReadEventDto> kafkaProducer;
    
    @Override
    public Mono<Void> publish(MessageReadEventDto event) {
        return kafkaProducer.send(
            KAFKA_TOPIC_MESSAGE_READ,
            getPartitionKey(event),
            event
        ).then();
    }
    
    private String getPartitionKey(MessageReadEventDto event) {
        return event.getSenderId() + ":" + event.getReceiverId();
    }
}
