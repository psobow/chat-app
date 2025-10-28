package com.sobow.chat.message.mapper;

import com.sobow.chat.message.domain.dto.ChatMessageHistoryResponseDto;
import com.sobow.chat.message.domain.entity.ChatMessage;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMessageMapper {
    
    @Mapping(
        target = "createdAt",
        expression = "java(mapLocalDateTime(chatMessage.getCreatedAt()))"
    )
    ChatMessageHistoryResponseDto toDto(ChatMessage chatMessage);
    
    default Instant mapLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toInstant(ZoneOffset.UTC);
    }
}
