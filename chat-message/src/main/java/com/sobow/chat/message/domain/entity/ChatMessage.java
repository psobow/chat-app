package com.sobow.chat.message.domain.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Table("chat_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage implements Persistable<UUID> {
    
    @Id
    private UUID id;
    
    private UUID senderId;
    
    private UUID receiverId;
    
    private String text;
    
    private Instant readAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Transient
    private boolean isNew;
}