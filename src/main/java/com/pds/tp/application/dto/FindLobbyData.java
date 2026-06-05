package com.pds.tp.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record FindLobbyData(
        @JsonAlias("juego") String game,
        String region,
        @JsonAlias("rangoMin") String minRank,
        @JsonAlias("rangoMax") String maxRank,
        @JsonAlias("fecha") String date,
        @JsonAlias("latenciaMax") Integer maxLatency
) {
}


