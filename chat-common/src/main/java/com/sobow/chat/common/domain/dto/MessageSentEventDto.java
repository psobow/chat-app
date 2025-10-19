package com.sobow.chat.common.domain.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageSentEventDto {
    
    private UUID messageId;
    private UUID senderId;
    private UUID receiverId;
    private String text;
}
