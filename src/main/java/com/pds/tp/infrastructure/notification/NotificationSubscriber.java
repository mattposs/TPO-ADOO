package com.pds.tp.infrastructure.notification;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import com.pds.tp.domain.event.ScrimCreatedEvent;
import com.pds.tp.domain.event.ScrimStateChangedEvent;
import com.pds.tp.infrastructure.repository.LobbyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Observer that reacts to scrim domain events and fans them out through all enabled channels.
 */
@Slf4j
@Component
public class NotificationSubscriber {
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 200L;

    private final NotifierFactory notifierFactory;
    private final LobbyRepository lobbyRepository;

    public NotificationSubscriber(NotifierFactory notifierFactory, LobbyRepository lobbyRepository) {
        this.notifierFactory = notifierFactory;
        this.lobbyRepository = lobbyRepository;
    }

    @EventListener
    public void onDomainEvent(ScrimStateChangedEvent event) {
        Notifier discord = notifierFactory.createDiscordNotifier();
        Notifier email = notifierFactory.createEmailNotifier();
        Notifier push = notifierFactory.createPushNotifier();
        Notifier ical = notifierFactory.createICalNotifier();

        String message = String.format("Lobby %s changed to state: %s",
                event.getLobbyId(), event.getNuevoEstado());
        NotificationTargets targets = resolveTargets(event.getLobbyId());

        // Send one event through all channels, then per-user targets for personalized channels.
        sendWithRetry(discord, "#scrim-updates", message, "DISCORD");
        for (String emailTarget : targets.emailTargets()) {
            sendWithRetry(email, emailTarget, message, "EMAIL");
        }
        for (String pushTarget : targets.pushTargets()) {
            sendWithRetry(push, pushTarget, message, "PUSH");
        }
        sendWithRetry(ical, "calendar@scrims.local", message, "ICAL");
    }

    @EventListener
    public void onScrimCreated(ScrimCreatedEvent event) {
        String message = String.format("New scrim created (%s) in %s. Lobby: %s",
                event.getGame(), event.getRegion(), event.getLobbyId());
        sendWithRetry(notifierFactory.createDiscordNotifier(), "#scrim-updates", message, "DISCORD");
        sendWithRetry(notifierFactory.createEmailNotifier(), "all-players@scrims.local", message, "EMAIL");
        sendWithRetry(notifierFactory.createPushNotifier(), "all-players", message, "PUSH");
        sendWithRetry(notifierFactory.createICalNotifier(), "calendar@scrims.local", message, "ICAL");
    }

    private void sendWithRetry(Notifier notifier, String target, String message, String channel) {
        long backoff = INITIAL_BACKOFF_MS;
        int attempt = 1;

        while (attempt <= MAX_ATTEMPTS) {
            try {
                notifier.sendNotification(target, message);
                return;
            } catch (RuntimeException ex) {
                if (attempt == MAX_ATTEMPTS) {
                    log.error("Could not deliver {} notification after {} attempts", channel, MAX_ATTEMPTS, ex);
                    return;
                }

                log.warn("Delivery failed via {} (attempt {}), retrying...", channel, attempt, ex);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrupted for channel {}", channel, interruptedException);
                    return;
                }
                backoff *= 2;
                attempt++;
            }
        }
    }

    private NotificationTargets resolveTargets(java.util.UUID lobbyId) {
        return lobbyRepository.findById(lobbyId)
                .map(this::toTargets)
                .orElseGet(() -> new NotificationTargets(
                        Set.of("all-players@scrims.local"),
                        Set.of("all-players")
                ));
    }

    private NotificationTargets toTargets(Lobby lobby) {
        Set<String> emailTargets = new LinkedHashSet<>();
        Set<String> pushTargets = new LinkedHashSet<>();

        if (lobby.getPlayers() != null) {
            for (Player player : lobby.getPlayers()) {
                if (player.getEmail() != null && !player.getEmail().isBlank()) {
                    emailTargets.add(player.getEmail());
                }
                if (player.getUsername() != null && !player.getUsername().isBlank()) {
                    pushTargets.add(player.getUsername());
                }
            }
        }

        // Fall back to broadcast targets so events are not dropped when a lobby has incomplete user data.
        if (emailTargets.isEmpty()) {
            emailTargets.add("all-players@scrims.local");
        }
        if (pushTargets.isEmpty()) {
            pushTargets.add("all-players");
        }

        return new NotificationTargets(emailTargets, pushTargets);
    }

    private record NotificationTargets(Set<String> emailTargets, Set<String> pushTargets) {
    }
}
