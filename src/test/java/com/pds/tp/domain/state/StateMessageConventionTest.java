package com.pds.tp.domain.state;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pds.tp.support.TestFixtures.lobby;
import static com.pds.tp.support.TestFixtures.player;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateMessageConventionTest {

    @Test
    void shouldRejectMisformattedStateMessageDefinitions() {
        assertThrows(IllegalArgumentException.class,
                () -> {
                    throw StateErrorStyle.invalidTransition("Sin punto final");
                });
    }

    @Test
    void shouldKeepTrailingDotConventionAcrossStateErrors() {
        Player host = player("host", "host@test.com", "LAS");
        Player guest = player("guest", "guest@test.com", "LAS");

        Lobby baseLobby = lobby(host, 1, 2, "Buscando", "BRONCE", "ORO", "ASCENT", List.of(host));

        assertMessageStyle(() -> new ScrimContext(baseLobby, new SearchingState(), null).iniciar());
        assertMessageStyle(() -> new ScrimContext(baseLobby, new CreatedLobbyState(), null).finalizar());
        assertMessageStyle(() -> new ScrimContext(baseLobby, new ConfirmedState(), null).confirmar(guest));
        assertMessageStyle(() -> new ScrimContext(baseLobby, new PlayingState(), null).cancelar());
        assertMessageStyle(() -> new ScrimContext(baseLobby, new FinishedState(), null).iniciar());
        assertMessageStyle(() -> new ScrimContext(baseLobby, new CanceledState(), null).postular(guest, "FLEX"));
    }

    private void assertMessageStyle(ThrowingOperation operation) {
        IllegalStateException exception = assertThrows(IllegalStateException.class, operation::run);
        assertFalse(exception.getMessage().isBlank());
        assertTrue(exception.getMessage().endsWith("."));
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run();
    }
}
