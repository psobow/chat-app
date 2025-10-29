package com.sobow.chat.realtime.service;

import com.sobow.chat.common.domain.dto.MessageSentPersistedEventDto;
import reactor.core.publisher.Flux;

public interface MessageSentPersistedEventConsumer {
    
    Flux<MessageSentPersistedEventDto> listen();
}
