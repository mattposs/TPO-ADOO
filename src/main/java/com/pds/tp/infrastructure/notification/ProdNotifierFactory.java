package com.pds.tp.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Production-oriented notifier factory that returns concrete provider adapters.
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "app.environment", havingValue = "prod", matchIfMissing = true)
public class ProdNotifierFactory implements NotifierFactory {

    private final DiscordAdapter discordAdapter;
    private final SendGridAdapter sendGridAdapter;
    private final ICalAdapter iCalAdapter;

    public ProdNotifierFactory(DiscordAdapter discordAdapter,
                               SendGridAdapter sendGridAdapter,
                               ICalAdapter iCalAdapter) {
        this.discordAdapter = discordAdapter;
        this.sendGridAdapter = sendGridAdapter;
        this.iCalAdapter = iCalAdapter;
    }

    @Override
    public Notifier createPushNotifier() {
        log.debug("Creating Push notifier (stub Firebase)");
        return (user, message) -> log.info("[Firebase Push Adapter Stub] {} -> {}", user, message);
    }

    @Override
    public Notifier createEmailNotifier() {
        log.debug("Creating Email notifier (SendGrid)");
        return sendGridAdapter;
    }

    @Override
    public Notifier createDiscordNotifier() {
        log.debug("Creating Discord notifier");
        return discordAdapter;
    }

    @Override
    public Notifier createICalNotifier() {
        log.debug("Creating iCal notifier");
        return iCalAdapter;
    }
}
