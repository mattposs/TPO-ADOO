package com.pds.tp.domain.state;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pds.tp.support.TestFixtures.lobby;
import static com.pds.tp.support.TestFixtures.player;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ScrimStateTransitionsTest {

    @Test
    void shouldFollowHappyPathTransitions() {
        Player host = player("host", "host@test.com", "LAS");
        Player guest = player("guest", "guest@test.com", "LAS");

        Lobby scrimLobby = lobby(host, 1, 2, "Buscando", "BRONCE", "ORO", "ASCENT", List.of(host));
        ScrimContext context = new ScrimContext(scrimLobby, new SearchingState(), null);

        context.postular(guest, "FLEX");
        assertEquals("LobbyArmado", context.getState().getStatusName());

        context.confirmar(host);
        context.confirmar(guest);
        assertEquals("Confirmado", context.getState().getStatusName());

        context.iniciar();
        assertEquals("EnJuego", context.getState().getStatusName());

        context.finalizar();
        assertEquals("Finalizado", context.getState().getStatusName());
    }

    @Test
    void shouldAllowCancelBeforePlaying() {
        Player host = player("host2", "host2@test.com", "LAS");

        Lobby scrimLobby = lobby(host, 1, 2, "Buscando", "BRONCE", "ORO", "BIND", List.of(host));
        ScrimContext context = new ScrimContext(scrimLobby, new SearchingState(), null);
        context.cancelar();

        assertEquals("Cancelado", context.getState().getStatusName());
    }
}
