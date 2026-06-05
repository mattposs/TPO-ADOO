package com.pds.tp.infrastructure.notification;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import com.pds.tp.domain.event.ScrimStateChangedEvent;
import com.pds.tp.infrastructure.repository.LobbyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.pds.tp.support.TestFixtures.lobby;
import static com.pds.tp.support.TestFixtures.player;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSubscriberTest {

    @Mock
    private NotifierFactory notifierFactory;

    @Mock
    private LobbyRepository lobbyRepository;

    @Mock
    private Notifier emailNotifier;

    @Mock
    private Notifier pushNotifier;

    @Mock
    private Notifier discordNotifier;

    @Mock
    private Notifier iCalNotifier;

    @Test
    void shouldNotifyEachLobbyMemberWhenAllPlayersConfirmed() {
        Player host = player("host", "host@test.com", "LAS");
        Player guest = player("guest", "guest@test.com", "LAS");

        UUID lobbyId = UUID.randomUUID();
        Lobby scrimLobby = lobby(host, 1, 2, "Confirmado", "BRONCE", "ORO", "HAVEN", List.of(host, guest));

        when(notifierFactory.createDiscordNotifier()).thenReturn(discordNotifier);
        when(notifierFactory.createEmailNotifier()).thenReturn(emailNotifier);
        when(notifierFactory.createPushNotifier()).thenReturn(pushNotifier);
        when(notifierFactory.createICalNotifier()).thenReturn(iCalNotifier);
        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(scrimLobby));

        NotificationSubscriber subscriber = new NotificationSubscriber(notifierFactory, lobbyRepository);
        subscriber.onDomainEvent(new ScrimStateChangedEvent(this, lobbyId, "Confirmado"));

        verify(emailNotifier, times(1)).sendNotification(eq("host@test.com"), contains("Confirmado"));
        verify(emailNotifier, times(1)).sendNotification(eq("guest@test.com"), contains("Confirmado"));
        verify(pushNotifier, times(1)).sendNotification(eq("host"), contains("Confirmado"));
        verify(pushNotifier, times(1)).sendNotification(eq("guest"), contains("Confirmado"));
        verify(discordNotifier, times(1)).sendNotification(eq("#scrim-updates"), contains("Confirmado"));
        verify(iCalNotifier, times(1)).sendNotification(eq("calendar@scrims.local"), contains("Confirmado"));
    }
}
