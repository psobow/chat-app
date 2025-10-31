package com.sobow.chat.message.service.impl;

import com.sobow.chat.message.domain.entity.ChatMessage;
import com.sobow.chat.message.repository.ChatMessageRepository;
import com.sobow.chat.message.service.ChatMessageService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    
    private final ChatMessageRepository chatMessageRepository;
    
    @Override
    public Mono<ChatMessage> save(ChatMessage chatMessage) {
        LocalDateTime now = LocalDateTime.now();
        chatMessage.setCreatedAt(now);
        chatMessage.setUpdatedAt(now);
        return chatMessageRepository.save(chatMessage);
    }
    
    @Override
    public Flux<ChatMessage> getChatMessageHistory(UUID currentUserId, UUID contactUserId) {
        return chatMessageRepository.findMessagesBetweenUsers(currentUserId, contactUserId);
    }
    
    @Override
    @Transactional
    public Mono<ChatMessage> markChatMessageAsRead(UUID messageId) {
        return chatMessageRepository
            .findById(messageId)
            .flatMap(message -> {
                message.setReadAt(Instant.now());
                return chatMessageRepository.save(message);
            });
    }
}
