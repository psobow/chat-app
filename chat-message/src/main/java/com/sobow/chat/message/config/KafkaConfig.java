package com.sobow.chat.message.config;

import static com.sobow.chat.common.KafkaTopics.KAFKA_TOPIC_MESSAGE_SENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.chat.common.domain.dto.MessageSentEventDto;
import com.sobow.chat.common.domain.dto.MessageSentPersistedEventDto;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

@Configuration
@EnableConfigurationProperties
@RequiredArgsConstructor
public class KafkaConfig {
    
    private final ObjectMapper objectMapper;
    
    @Bean
    public ReactiveKafkaProducerTemplate<String, MessageSentPersistedEventDto> kafkaProducerMessageSentPersistedEvent(
        KafkaProperties kafkaProperties
    ) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        
        // Create SenderOptions with custom serializers
        SenderOptions<String, MessageSentPersistedEventDto> senderOptions =
            SenderOptions.<String, MessageSentPersistedEventDto>create(props)
                         .withValueSerializer(new JsonSerializer<>(objectMapper));
        
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }
    
    @Bean
    public ReactiveKafkaConsumerTemplate<String, MessageSentEventDto> kafkaConsumerMessageSentEvent(
        KafkaProperties kafkaProperties
    ) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        
        JsonDeserializer<MessageSentEventDto> jsonDeserializer =
            new JsonDeserializer<>(MessageSentEventDto.class, objectMapper);
        jsonDeserializer.addTrustedPackages("com.sobow.chat.common.domain.dto");
        
        ReceiverOptions<String, MessageSentEventDto> receiverOptions =
            ReceiverOptions.<String, MessageSentEventDto>create(props)
                           .withValueDeserializer(jsonDeserializer)
                           .subscription(Collections.singleton(KAFKA_TOPIC_MESSAGE_SENT));
        
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }
}