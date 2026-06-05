package com.pds.tp.application.dto;

import java.util.UUID;

public record ReportConfirmation(
        UUID reportId,
        String reportingPlayerUsername,
        String lobbyId,
        String reportedPlayerUsername,
        String status
) {
}


