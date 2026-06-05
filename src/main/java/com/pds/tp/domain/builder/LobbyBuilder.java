package com.pds.tp.domain.builder;

import com.pds.tp.domain.entity.Player;

import java.time.LocalDateTime;

/**
 * Backward-compatible wrapper that keeps Spanish fluent names.
 * Core build logic lives in ScrimBuilder to avoid duplicated responsibilities.
 */
public class LobbyBuilder extends ScrimBuilder {

    public LobbyBuilder conHost(Player host) {
        super.host(host);
        return this;
    }

    public LobbyBuilder conRegion(String region) {
        super.region(region);
        return this;
    }

    public LobbyBuilder conFecha(LocalDateTime scheduledTime) {
        super.fecha(scheduledTime);
        return this;
    }

    public LobbyBuilder conFormato(int minPlayers, int maxPlayers) {
        super.formato(minPlayers, maxPlayers);
        return this;
    }

    public LobbyBuilder conRango(String minRank, String maxRank) {
        super.rango(minRank, maxRank);
        return this;
    }

    public LobbyBuilder conJuego(String gameMode, String map) {
        super.juego(gameMode, map);
        return this;
    }

    public LobbyBuilder conLatenciaMax(int maxPing) {
        super.latenciaMax(maxPing);
        return this;
    }
}
