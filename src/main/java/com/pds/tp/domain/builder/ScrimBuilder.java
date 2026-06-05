package com.pds.tp.domain.builder;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Builder used by services to assemble a Lobby/Scrim setup with business defaults.
 */
public class ScrimBuilder {
    private LocalDateTime scheduledTime = LocalDateTime.now();
    private int maxPlayers;
    private int minPlayers;
    private String region;
    private String minRank;
    private String maxRank;
    private int maxPing;
    private String gameMode;
    private String map;
    private Player host;

    public ScrimBuilder host(Player host) {
        this.host = host;
        this.region = host != null ? host.getRegion() : null;
        return this;
    }

    public ScrimBuilder region(String region) {
        if (region != null && !region.isBlank()) {
            this.region = region;
        }
        return this;
    }

    public ScrimBuilder fecha(LocalDateTime scheduledTime) {
        if (scheduledTime != null) {
            this.scheduledTime = scheduledTime;
        }
        return this;
    }

    public ScrimBuilder formato(int minPlayers, int maxPlayers) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        return this;
    }

    public ScrimBuilder rango(String minRank, String maxRank) {
        this.minRank = minRank;
        this.maxRank = maxRank;
        return this;
    }

    public ScrimBuilder juego(String gameMode, String map) {
        this.gameMode = gameMode;
        this.map = map;
        return this;
    }

    public ScrimBuilder latenciaMax(int maxPing) {
        this.maxPing = maxPing;
        return this;
    }

    public Lobby build() {
        if (host == null) {
            throw new IllegalStateException("El host es requerido para crear el lobby.");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalStateException("La region es requerida para crear el lobby.");
        }
        if (maxPlayers < minPlayers) {
            throw new IllegalStateException("El maximo de jugadores no puede ser menor al minimo.");
        }

        ArrayList<Player> initialPlayers = new ArrayList<>();
        initialPlayers.add(host);

        return new Lobby(
                scheduledTime,
                maxPlayers,
                minPlayers,
                region,
                minRank,
                maxRank,
                maxPing,
                gameMode,
                map,
                "Buscando",
                host,
                initialPlayers,
                new HashSet<>()

        );
    }
}
