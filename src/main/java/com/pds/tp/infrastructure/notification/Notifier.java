package com.pds.tp.infrastructure.notification;

/**
 * Common contract for channel adapters (email, push, Discord, iCal, etc.).
 */
public interface Notifier {
    void sendNotification(String channelOrUser, String message);
}
