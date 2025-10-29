package com.sobow.chat.message.service;

import com.sobow.chat.common.domain.dto.MessageSentPersistedEventDto;
import reactor.core.publisher.Mono;

public interface MessageSentPersistedEventPublisher {
    
    Mono<Void> publish(MessageSentPersistedEventDto event);
}
