package com.pds.tp.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter for email delivery through SendGrid.
 * Current implementation logs the payload as a production-safe stub.
 */
@Slf4j
@Component
public class SendGridAdapter implements Notifier {
    @Override
    public void sendNotification(String emailAddress, String message) {
        // Real implementation would send this message through the SendGrid SDK/API.
        log.info("[SendGrid API] Enviando email a {}. Cuerpo: {}", emailAddress, message);
    }
}
