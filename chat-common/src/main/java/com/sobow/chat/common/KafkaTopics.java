package com.sobow.chat.common;

public final class KafkaTopics {
    
    public static final String KAFKA_TOPIC_MESSAGE_SENT = "message.sent";
    public static final String KAFKA_TOPIC_MESSAGE_SENT_PERSISTED = "message.sent.persisted";
    
    public static final String KAFKA_TOPIC_MESSAGE_READ = "message.read";
    public static final String KAFKA_TOPIC_MESSAGE_READ_PERSISTED = "message.read.persisted";
    
    private KafkaTopics() {
    }
}
