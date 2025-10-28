package com.sobow.chat.message.service;

import com.sobow.chat.message.domain.entity.ChatMessage;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatMessageService {
    
    Mono<ChatMessage> save(ChatMessage chatMessage);
    
    Flux<ChatMessage> getChatMessageHistory(UUID currentUserId, UUID contactUserId);
}
