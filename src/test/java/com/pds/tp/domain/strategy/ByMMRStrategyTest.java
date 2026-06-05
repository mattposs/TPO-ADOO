package com.pds.tp.domain.strategy;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.pds.tp.support.TestFixtures.lobby;
import static com.pds.tp.support.TestFixtures.player;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ByMMRStrategyTest {

    @Test
    void shouldSelectOnlyPlayersWithinRankRange() {
        Player host = player("host", "host@mmr.com", "LAS");
        Lobby scrimLobby = lobby(host, 1, 3, "Buscando", "PLATA", "ORO", "HAVEN", List.of(host));

        Player bronze = player("bronze", "b@x.com", "LAS");
        Player silver = player("silver", "s@x.com", "LAS");
        Player gold = player("gold", "g@x.com", "LAS");
        Player radiant = player("radiant", "r@x.com", "LAS");

        bronze.setVisibleRank("BRONCE");
        silver.setVisibleRank("PLATA");
        gold.setVisibleRank("ORO");
        radiant.setVisibleRank("RADIANTE");

        List<Player> selected = new ByMMRStrategy().seleccionar(List.of(bronze, silver, gold, radiant), scrimLobby);

        assertEquals(2, selected.size());
        assertTrue(selected.contains(silver));
        assertTrue(selected.contains(gold));
    }

    @Test
    void shouldProcess500CandidatesUnderTwoSeconds() {
        Player host = player("host2", "host2@mmr.com", "LAS");
        Lobby scrimLobby = lobby(host, 5, 10, "Buscando", "BRONCE", "RADIANTE", "LOTUS", List.of(host));

        List<Player> candidates = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Player candidate = player("p" + i, "p" + i + "@x.com", "LAS");
            candidate.setVisibleRank(i % 2 == 0 ? "ORO" : "PLATA");
            candidates.add(candidate);
        }

        long start = System.nanoTime();
        new ByMMRStrategy().seleccionar(candidates, scrimLobby);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMillis < 2000, "Matchmaking exceeded 2 seconds");
    }
}
