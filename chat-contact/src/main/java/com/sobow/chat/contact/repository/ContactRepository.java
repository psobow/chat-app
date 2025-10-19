package com.sobow.chat.contact.repository;

import com.sobow.chat.contact.domain.entity.Contact;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ContactRepository extends ReactiveCrudRepository<Contact, UUID> {
    
    Flux<Contact> findAllByOwnerId(UUID ownerId);
    
    Mono<Contact> findByOwnerIdAndContactUserId(UUID ownerId, UUID contactUserId);
}
