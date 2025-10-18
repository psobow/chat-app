package com.sobow.chat.contact.service.impl;

import com.sobow.chat.contact.domain.entity.Contact;
import com.sobow.chat.contact.repository.ContactRepository;
import com.sobow.chat.contact.service.ContactService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    
    private final ContactRepository contactRepository;
    
    @Override
    public Flux<Contact> findAllForUser(UUID userId) {
        return contactRepository.findAllByOwnerId(userId);
    }
}
