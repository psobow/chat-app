package com.sobow.chat.realtime.service;

import com.sobow.chat.common.domain.dto.MessageReadEventDto;
import reactor.core.publisher.Mono;

public interface MessageReadEventPublisher {
    
    Mono<Void> publish(MessageReadEventDto event);
}
