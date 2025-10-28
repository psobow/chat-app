package com.sobow.chat.message.controller;

import com.sobow.chat.message.domain.dto.ChatMessageHistoryResponseDto;
import com.sobow.chat.message.mapper.ChatMessageMapper;
import com.sobow.chat.message.service.ChatMessageService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {
    
    private final ChatMessageService chatMessageService;
    private final ChatMessageMapper chatMessageMapper;
    
    @GetMapping(path = "/history/{recipientId}")
    public Mono<ResponseEntity<List<ChatMessageHistoryResponseDto>>> getChatMessageHistory(
        @RequestHeader("X-User-ID") UUID userId,
        @PathVariable UUID recipientId
    ) {
        return chatMessageService
            .getChatMessageHistory(userId, recipientId)
            .map(chatMessageMapper::toDto)
            .collectList()
            .map(ResponseEntity::ok)
            .doOnError(throwable ->
                           log.error("Unable to list message history between user {} and {}",
                                     userId,
                                     recipientId,
                                     throwable.getMessage())
            );
    }
}
