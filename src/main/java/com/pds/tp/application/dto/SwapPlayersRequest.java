package com.pds.tp.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record SwapPlayersRequest(
        @JsonAlias("jugador1") String firstPlayerUsername,
        @JsonAlias("jugador2") String secondPlayerUsername
) {
}

