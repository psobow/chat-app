package com.sobow.chat.contact.controller;

import com.sobow.chat.contact.domain.dto.ContactResponseDto;
import com.sobow.chat.contact.domain.dto.CreateContactRequestDto;
import com.sobow.chat.contact.domain.dto.CreateContactResponseDto;
import com.sobow.chat.contact.domain.entity.Contact;
import com.sobow.chat.contact.mapper.ContactMapper;
import com.sobow.chat.contact.service.ContactService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {
    
    private final ContactService contactService;
    private final ContactMapper contactMapper;
    
    @GetMapping
    public Flux<ResponseEntity<ContactResponseDto>> listContacts(
        @RequestHeader("X-User-ID") UUID userId
    ) {
        Flux<Contact> contacts = contactService.findAllForUser(userId);
        Flux<ContactResponseDto> responseDtoFlux = contacts.map(contactMapper::toContactResponseDto);
        return responseDtoFlux.map(ResponseEntity::ok);
    }
    
    @PostMapping
    public Mono<ResponseEntity<CreateContactResponseDto>> createContact(
        @RequestHeader("X-User-ID") UUID userId,
        @RequestBody CreateContactRequestDto createContactRequestDto
    ) {
        Mono<CreateContactResponseDto> responseDto = contactService.createContact(
            userId,
            createContactRequestDto.getContactUserId(),
            createContactRequestDto.getDisplayName()
        ).map(contactMapper::toCreateContactResponseDto);
        
        return responseDto.map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }
}
