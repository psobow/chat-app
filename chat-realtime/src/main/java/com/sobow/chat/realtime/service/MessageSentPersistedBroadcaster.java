package com.sobow.chat.realtime.service;

import com.sobow.chat.common.domain.dto.MessageSentPersistedEventDto;
import reactor.core.publisher.Flux;

public interface MessageSentPersistedBroadcaster {
    
    Flux<MessageSentPersistedEventDto> subscribe();
}
