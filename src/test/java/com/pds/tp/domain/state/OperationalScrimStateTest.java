package com.pds.tp.domain.state;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pds.tp.support.TestFixtures.lobby;
import static com.pds.tp.support.TestFixtures.player;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OperationalScrimStateTest {

    @Test
    void confirmedStateShouldKeepValidationMessagesAndAllowedTransitions() {
        ScrimContext context = new ScrimContext(buildLobby(), new ConfirmedState(), null);
        Player candidate = player("p1", "p1@test.com", "LAS");

        assertEquals("Lobby is full and already confirmed.", assertThrows(IllegalStateException.class, () -> context.postular(candidate, "FLEX")).getMessage());
        assertEquals("All players are already confirmed.", assertThrows(IllegalStateException.class, () -> context.confirmar(candidate)).getMessage());
        assertEquals("Scrim must start before it can be finished.", assertThrows(IllegalStateException.class, context::finalizar).getMessage());

        context.cancelar();
        assertEquals("Cancelado", context.getState().getStatusName());
    }

    @Test
    void playingStateShouldKeepValidationMessagesAndAllowedTransitions() {
        ScrimContext context = new ScrimContext(buildLobby(), new PlayingState(), null);
        Player candidate = player("p2", "p2@test.com", "LAS");

        assertEquals("Scrim is in progress.", assertThrows(IllegalStateException.class, () -> context.postular(candidate, "FLEX")).getMessage());
        assertEquals("Scrim is in progress.", assertThrows(IllegalStateException.class, () -> context.confirmar(candidate)).getMessage());
        assertEquals("Scrim has already started.", assertThrows(IllegalStateException.class, context::iniciar).getMessage());
        assertEquals("Cannot cancel a scrim that is already in progress; it must be finished.", assertThrows(IllegalStateException.class, context::cancelar).getMessage());

        context.finalizar();
        assertEquals("Finalizado", context.getState().getStatusName());
    }

    private Lobby buildLobby() {
        Player host = player("host", "host@test.com", "LAS");
        return lobby(host, 1, 2, "Buscando", "BRONCE", "ORO", "ASCENT", List.of(host));
    }
}
