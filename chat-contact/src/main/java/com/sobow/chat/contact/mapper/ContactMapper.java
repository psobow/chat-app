package com.sobow.chat.contact.mapper;

import com.sobow.chat.contact.domain.dto.ContactResponseDto;
import com.sobow.chat.contact.domain.dto.CreateContactResponseDto;
import com.sobow.chat.contact.domain.entity.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContactMapper {
    
    @Mapping(source = "contactUserId", target = "userId")
    ContactResponseDto toContactResponseDto(Contact contact);
    
    @Mapping(source = "contactUserId", target = "userId")
    CreateContactResponseDto toCreateContactResponseDto(Contact contact);
}
