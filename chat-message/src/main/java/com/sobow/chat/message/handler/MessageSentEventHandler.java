package com.sobow.chat.message.handler;

import com.sobow.chat.common.domain.dto.MessageSentEventDto;
import com.sobow.chat.common.domain.dto.MessageSentPersistedEventDto;
import com.sobow.chat.message.domain.entity.ChatMessage;
import com.sobow.chat.message.service.ChatMessageService;
import com.sobow.chat.message.service.MessageSentPersistedEventPublisher;
import jakarta.annotation.PostConstruct;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageSentEventHandler {
    
    private final ReactiveKafkaConsumerTemplate<String, MessageSentEventDto> kafkaConsumer;
    private final ChatMessageService chatMessageService;
    private final MessageSentPersistedEventPublisher messageSentEventPublisher;
    
    @PostConstruct
    public void startKafkaConsumer() {
        kafkaConsumer
            .receive()
            .doOnSubscribe(subscription -> log.info("Message sent consumer started successfully"))
            .doOnNext(event -> log.debug("Message sent event received : {}", event))
            .flatMap(event -> processMessageSentEvent(event.value())
                .onErrorResume(error -> {
                    log.error("Failed to process message sent event {}", event, error);
                    return Mono.empty();
                }))
            .doOnError(error -> log.error("Error in Kafka consumer", error))
            .retry(3)
            .onErrorContinue((error, obj) -> log.error("Continuing after error: {}", error.getMessage()))
            .subscribe();
    }
    
    private Mono<Void> processMessageSentEvent(MessageSentEventDto messageSentEventDto) {
        ChatMessage chatMessage = ChatMessage.builder()
                                             .id(messageSentEventDto.getMessageId())
                                             .senderId(messageSentEventDto.getSenderId())
                                             .receiverId(messageSentEventDto.getReceiverId())
                                             .text(messageSentEventDto.getText())
                                             .readAt(null)
                                             .isNew(true)
                                             .build();
        
        return chatMessageService.save(chatMessage)
                                 .flatMap(this::publishMessageSentPersistedEvent)
                                 .then();
    }
    
    private Mono<Void> publishMessageSentPersistedEvent(ChatMessage chatMessage) {
        MessageSentPersistedEventDto event = MessageSentPersistedEventDto
            .builder()
            .messageId(chatMessage.getId())
            .senderId(chatMessage.getSenderId())
            .receiverId(chatMessage.getReceiverId())
            .text(chatMessage.getText())
            .persistedAt(chatMessage.getCreatedAt().toInstant(ZoneOffset.UTC))
            .build();
        return messageSentEventPublisher.publish(event);
    }
}
