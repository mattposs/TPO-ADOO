package com.pds.tp.application.dto;

public record LobbyConfirmation(
        String playerId,
        String lobbyId,
        String status,
        String detail
) {
}


