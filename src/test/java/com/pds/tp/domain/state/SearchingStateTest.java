package com.pds.tp.domain.state;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pds.tp.support.TestFixtures.lobby;
import static com.pds.tp.support.TestFixtures.player;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchingStateTest {

    @Test
    void shouldRejectPostulationWhenPlayerRankIsOutsideConfiguredRange() {
        Player host = player("host", "host@state.com", "LAS");
        Player silver = player("silver", "silver@state.com", "LAS");

        host.setVisibleRank("ORO");
        silver.setVisibleRank("PLATA");

        Lobby scrimLobby = lobby(host, 1, 2, "Buscando", "ORO", "PLATINO", "BIND", List.of(host));
        ScrimContext context = new ScrimContext(scrimLobby, new SearchingState(), null);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> context.postular(silver, "FLEX"));

        assertTrue(thrown.getMessage().contains("rank"));
    }
}
