package com.pds.tp.support;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Shared test fixtures to keep domain/service tests concise and consistent.
 */
public final class TestFixtures {
    private static final String DEFAULT_PASSWORD = "pwd";
    private static final String DEFAULT_ROLE = "FLEX";
    private static final String DEFAULT_PLATFORM = "PC";
    private static final String DEFAULT_AVAILABILITY = "NOCHE";
    private static final String DEFAULT_GAME = "VALORANT";
    private static final int DEFAULT_MAX_PING = 80;

    private TestFixtures() {
    }

    public static Player player(String username, String email, String region) {
        Player player = new Player(username, email, DEFAULT_PASSWORD, DEFAULT_ROLE, region, DEFAULT_PLATFORM, DEFAULT_AVAILABILITY);
        setId(player);
        return player;
    }

    public static Lobby lobby(
            Player host,
            int minPlayers,
            int maxPlayers,
            String status,
            String minRank,
            String maxRank,
            String map,
            List<Player> players
    ) {
        List<Player> lobbyPlayers = players == null || players.isEmpty()
                ? new ArrayList<>(List.of(host))
                : new ArrayList<>(players);

        return new Lobby(
                LocalDateTime.now().plusHours(1),
                maxPlayers,
                minPlayers,
                host.getRegion(),
                minRank,
                maxRank,
                DEFAULT_MAX_PING,
                DEFAULT_GAME,
                map,
                status,
                host,
                lobbyPlayers,
                new HashSet<>()
        );
    }

    public static void setId(Object entity) {
        setId(entity, UUID.randomUUID());
    }

    public static void setId(Object entity, UUID id) {
        setField(entity, "id", id);
    }

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

