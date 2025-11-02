package com.sobow.chat.realtime.service;

import com.sobow.chat.common.domain.dto.MessageReadPersistedEventDto;
import reactor.core.publisher.Flux;

public interface MessageReadPersistedEventBroadcaster {
    
    Flux<MessageReadPersistedEventDto> subscribe();
}
