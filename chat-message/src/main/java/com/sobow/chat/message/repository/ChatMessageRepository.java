package com.sobow.chat.message.repository;

import com.sobow.chat.message.domain.entity.ChatMessage;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, UUID> {
    
    @Query("""
        SELECT * FROM chat_messages
        WHERE sender_id IN (:user1, :user2)
            AND receiver_id IN (:user1, :user2)
            AND sender_id != receiver_id
        ORDER BY created_at ASC
        """)
    Flux<ChatMessage> findMessagesBetweenUsers(UUID user1, UUID user2);
}
