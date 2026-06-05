package com.pds.tp.application.dto;

public record PlayerData(
        String playerName,
        String email,
        String password,
        String preferredRole,
        String region,
        String platform,
        String availability
) {
}


