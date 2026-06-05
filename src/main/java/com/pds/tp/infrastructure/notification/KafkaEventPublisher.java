package com.pds.tp.infrastructure.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String scrimEventsTopic;

    public KafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topics.scrim-events}") String scrimEventsTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.scrimEventsTopic = scrimEventsTopic;
    }

    public void publish(String key, String payload) {
        kafkaTemplate.send(scrimEventsTopic, key, payload);
    }
}