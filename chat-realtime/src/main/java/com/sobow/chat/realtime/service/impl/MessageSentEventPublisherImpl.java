package com.sobow.chat.realtime.service.impl;

import static com.sobow.chat.common.KafkaTopics.KAFKA_TOPIC_MESSAGE_SENT;

import com.sobow.chat.common.domain.dto.MessageSentEventDto;
import com.sobow.chat.realtime.service.MessageSentEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MessageSentEventPublisherImpl implements MessageSentEventPublisher {
    
    private final ReactiveKafkaProducerTemplate<String, MessageSentEventDto> kafkaProducer;
    
    @Override
    public Mono<Void> publish(MessageSentEventDto messageSentEventDto) {
        return kafkaProducer.send(
            KAFKA_TOPIC_MESSAGE_SENT,
            getPartitionKey(messageSentEventDto),
            messageSentEventDto
        ).then();
    }
    
    private String getPartitionKey(MessageSentEventDto messageSentEventDto) {
        return messageSentEventDto.getSenderId() + ":" + messageSentEventDto.getReceiverId();
    }
}
