package com.sobow.chat.message.service;

import com.sobow.chat.message.domain.entity.ChatMessage;
import reactor.core.publisher.Mono;

public interface ChatMessageService {
    
    Mono<ChatMessage> save(ChatMessage chatMessage);
}
