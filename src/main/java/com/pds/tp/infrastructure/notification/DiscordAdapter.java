package com.pds.tp.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter for Discord webhook delivery.
 * Current implementation logs the payload as a production-safe stub.
 */
@Slf4j
@Component
public class DiscordAdapter implements Notifier {
    @Override
    public void sendNotification(String channel, String message) {
        // Real implementation would POST this payload to a configured Discord webhook URL.
        String jsonPayload = String.format("{\"content\": \"%s\"}", message);
        log.info("[Discord API] Ejecutando HTTP POST al webhook del canal {}: {}", channel, jsonPayload);
    }
}
