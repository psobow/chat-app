package com.sobow.chat.contact.domain.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateContactResponseDto {
    
    private UUID id;
    private UUID userId;
    private String displayName;
}
