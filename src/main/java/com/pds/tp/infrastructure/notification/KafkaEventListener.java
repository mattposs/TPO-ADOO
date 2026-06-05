package com.pds.tp.infrastructure.notification;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventListener {

    @KafkaListener(
            topics = "${app.kafka.topics.scrim-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(String payload) {
        // Replace with real domain handling (application service call).
        System.out.println("Received Kafka event: " + payload);
    }
}