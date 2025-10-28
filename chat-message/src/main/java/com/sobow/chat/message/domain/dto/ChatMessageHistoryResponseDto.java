package com.sobow.chat.message.domain.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageHistoryResponseDto {
    
    private UUID id;
    
    private UUID senderId;
    
    private UUID receiverId;
    
    private String text;
    
    private Instant readAt;
    
    private Instant createdAt;
    
    private Instant updatedAt;
}
