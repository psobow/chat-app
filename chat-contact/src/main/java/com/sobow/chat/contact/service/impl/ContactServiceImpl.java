package com.sobow.chat.contact.service.impl;

import com.sobow.chat.contact.domain.entity.Contact;
import com.sobow.chat.contact.repository.ContactRepository;
import com.sobow.chat.contact.service.ContactService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    
    private final ContactRepository contactRepository;
    
    @Override
    public Flux<Contact> findAllForUser(UUID userId) {
        return contactRepository.findAllByOwnerId(userId);
    }
    
    @Override
    public Mono<Contact> createContact(UUID ownerId, UUID contactUserId, String displayName) {
        return contactRepository.findByOwnerIdAndContactUserId(ownerId, contactUserId)
                                .switchIfEmpty(createNewContact(ownerId, contactUserId, displayName));
    }
    
    private Mono<Contact> createNewContact(UUID ownerId, UUID contactUserId, String displayName) {
        LocalDateTime now = LocalDateTime.now();
        Contact contact = Contact.builder()
                                 .id(UUID.randomUUID())
                                 .ownerId(ownerId)
                                 .contactUserId(contactUserId)
                                 .displayName(displayName)
                                 .createdAt(now)
                                 .updatedAt(now)
                                 .isNew(true)
                                 .build();
        return contactRepository.save(contact);
    }
}
