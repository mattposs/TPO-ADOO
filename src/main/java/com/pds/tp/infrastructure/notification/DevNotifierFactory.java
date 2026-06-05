package com.pds.tp.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Development notifier factory that logs outbound notifications instead of calling providers.
 */
@Slf4j
@Component
@Profile("dev")
public class DevNotifierFactory implements NotifierFactory {
    @Override
    public Notifier createPushNotifier() {
        return (user, message) -> log.info("[DEV-PUSH] {} -> {}", user, message);
    }

    @Override
    public Notifier createEmailNotifier() {
        return (user, message) -> log.info("[DEV-EMAIL] {} -> {}", user, message);
    }

    @Override
    public Notifier createDiscordNotifier() {
        return (user, message) -> log.info("[DEV-DISCORD] {} -> {}", user, message);
    }

    @Override
    public Notifier createICalNotifier() {
        return (user, message) -> log.info("[DEV-ICAL] {} -> {}", user, message);
    }
}
