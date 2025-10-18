package com.sobow.chat.contact.mapper;

import com.sobow.chat.contact.domain.dto.ContactResponseDto;
import com.sobow.chat.contact.domain.entity.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContactMapper {
    
    @Mapping(source = "contactUserId", target = "userId")
    ContactResponseDto toDto(Contact contact);
}
