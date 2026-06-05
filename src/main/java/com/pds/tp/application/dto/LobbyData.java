package com.pds.tp.application.dto;

public record LobbyData(String scheduledTime,
                        int maxPlayers,
                        int minPlayers,
                        String minRank,
                        String maxRank,
                        int maxPing,
                        String gameMode,
                        String map,
                        String hostUserName) {
}


