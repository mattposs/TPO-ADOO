package com.pds.tp.domain.strategy;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pds.tp.support.TestFixtures.lobby;
import static com.pds.tp.support.TestFixtures.player;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ByLatencyStrategyTest {

    @Test
    void shouldRejectPlayersAboveLobbyLatencyThreshold() {
        Player host = player("host", "host@lat.com", "LAS");
        Player fast = player("fast", "fast@lat.com", "LAS");
        Player slow = player("slow", "slow@lat.com", "LAS");
        Player otherRegion = player("other", "other@lat.com", "LAN");

        fast.setAveragePingMs(40);
        slow.setAveragePingMs(120);
        otherRegion.setAveragePingMs(20);

        Lobby scrimLobby = lobby(host, 1, 4, "Buscando", "BRONCE", "RADIANTE", "HAVEN", List.of(host));

        List<Player> selected = new ByLatencyStrategy().seleccionar(List.of(fast, slow, otherRegion), scrimLobby);

        assertEquals(1, selected.size());
        assertTrue(selected.contains(fast));
        assertFalse(selected.contains(slow));
        assertFalse(selected.contains(otherRegion));
    }
}
