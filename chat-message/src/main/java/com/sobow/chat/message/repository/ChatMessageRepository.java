package com.sobow.chat.message.repository;

import com.sobow.chat.message.domain.entity.ChatMessage;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, UUID> {

}
