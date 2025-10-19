package com.sobow.chat.realtime.exception;

public class WebSocketUnauthorizedException extends RuntimeException {
    
    public WebSocketUnauthorizedException() {
    }
    
    public WebSocketUnauthorizedException(String message) {
        super(message);
    }
    
    public WebSocketUnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WebSocketUnauthorizedException(Throwable cause) {
        super(cause);
    }
}
