package com.pds.tp.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CreateScrimRequest(
        @JsonAlias("juego") String game,
        @JsonAlias("formato") String format,
        @JsonAlias("cantidadJugadoresPorLado") int playersPerSide,
        @JsonAlias("cantidadTotalJugadores") int totalPlayers,
        String region,
        @JsonAlias("rangoMin") String minRank,
        @JsonAlias("rangoMax") String maxRank,
        @JsonAlias("latenciaMax") int maxLatency,
        @JsonAlias("fecha") String scheduledDate,
        @JsonAlias("duracion") String duration,
        @JsonAlias("modalidad") String mode,
        @JsonAlias("mapa") String map,
        String hostUserName
) {
}

