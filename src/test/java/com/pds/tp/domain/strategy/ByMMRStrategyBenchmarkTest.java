package com.pds.tp.domain.strategy;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.pds.tp.support.TestFixtures.lobby;
import static com.pds.tp.support.TestFixtures.player;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lightweight benchmark guard for matchmaking performance requirement.
 */
class ByMMRStrategyBenchmarkTest {

    @Test
    void shouldSelectPlayersBelowTwoSecondsFor500Candidates() {
        ByMMRStrategy strategy = new ByMMRStrategy();
        Lobby scrimLobby = createTestLobby();
        List<Player> candidates = generateCandidates(500);

        long start = System.nanoTime();
        List<Player> selected = strategy.seleccionar(candidates, scrimLobby);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMillis < 2000, "Emparejamiento tardo " + elapsedMillis + "ms, esperado < 2000ms");
        assertTrue(!selected.isEmpty(), "Debe seleccionar al menos un candidato");
    }

    private Lobby createTestLobby() {
        Player host = player("host-bench", "host-bench@test.com", "LAS");
        host.setVisibleRank("PLATA");
        return lobby(host, 1, 10, "Buscando", "BRONCE", "DIAMANTE", "HAVEN", List.of(host));
    }

    private List<Player> generateCandidates(int count) {
        String[] ranks = {"BRONCE", "PLATA", "ORO", "PLATINO", "DIAMANTE"};
        Random random = new Random(42);

        List<Player> players = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Player candidate = player("player" + i, "player" + i + "@test.com", "LAS");
            candidate.setVisibleRank(ranks[random.nextInt(ranks.length)]);
            players.add(candidate);
        }
        return players;
    }
}
