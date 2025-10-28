package com.sobow.chat.realtime.service;

import com.sobow.chat.common.domain.dto.MessageSentEventDto;
import reactor.core.publisher.Mono;

public interface MessageSentEventPublisher {
    
    Mono<Void> publish(MessageSentEventDto messageSentEventDto);
}
