package com.pds.tp.infrastructure.notification;

/**
 * Abstract Factory for notification channels.
 * Implementations decide whether to create real integrations or local/dev stubs.
 */
public interface NotifierFactory {
    Notifier createPushNotifier();

    Notifier createEmailNotifier();

    Notifier createDiscordNotifier();

    Notifier createICalNotifier();
}
