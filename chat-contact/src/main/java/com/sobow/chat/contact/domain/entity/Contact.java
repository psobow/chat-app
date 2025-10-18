package com.sobow.chat.contact.domain.entity;

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

@Table("contacts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Contact implements Persistable<UUID> {
    
    @Id
    private UUID id;
    
    private UUID ownerId;
    
    private UUID contactUserId;
    
    private String displayName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Transient
    private boolean isNew;
}
