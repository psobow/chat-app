package com.sobow.chat.contact.service;

import com.sobow.chat.contact.domain.entity.Contact;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContactService {
    
    Flux<Contact> findAllForUser(UUID userId);
    
    Mono<Contact> createContact(UUID ownerId, UUID contactUserId, String displayName);
}
