package com.sobow.chat.message.handler;

import com.sobow.chat.common.domain.dto.MessageReadEventDto;
import com.sobow.chat.message.service.ChatMessageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageReadEventHandler {
    
    private final ReactiveKafkaConsumerTemplate<String, MessageReadEventDto> kafkaConsumer;
    
    private final ChatMessageService chatMessageService;
    
    @PostConstruct
    public void startKafkaConsumer() {
        kafkaConsumer.receive()
                     .doOnSubscribe(subscription -> log.info("Message read event consumer started"))
                     .doOnNext(next -> log.debug("Received message read event: {}", next.value()))
                     .flatMap(event ->
                                  processMessageReadEvent(event.value()).onErrorResume(throwable -> {
                                      log.error("Failed to process message read event {}", event.value(), throwable);
                                      return Mono.empty();
                                  }))
                     .doOnError(error -> log.error("Error in Kafka consumer", error))
                     .retry(3)
                     .onErrorContinue((error, object) -> log.error("Continuing after error", error))
                     .subscribe();
    }
    
    private Mono<Void> processMessageReadEvent(MessageReadEventDto messageReadEventDto) {
        return chatMessageService.markChatMessageAsRead(messageReadEventDto.getMessageId()).then();
    }
}
