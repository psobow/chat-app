package com.sobow.chat.realtime.domain.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebSocketEventEnvelope<T> {
    
    private UUID id;
    private WebSocketEventType type;
    private T payload;
    private Instant timestamp;
}
