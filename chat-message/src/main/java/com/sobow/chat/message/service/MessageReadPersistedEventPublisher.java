package com.sobow.chat.message.service;

import com.sobow.chat.common.domain.dto.MessageReadPersistedEventDto;
import reactor.core.publisher.Mono;

public interface MessageReadPersistedEventPublisher {
    
    Mono<Void> publish(MessageReadPersistedEventDto event);
}
