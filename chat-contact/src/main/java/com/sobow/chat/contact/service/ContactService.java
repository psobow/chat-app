package com.sobow.chat.contact.service;

import com.sobow.chat.contact.domain.entity.Contact;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ContactService {
    
    Flux<Contact> findAllForUser(UUID userId);
}
