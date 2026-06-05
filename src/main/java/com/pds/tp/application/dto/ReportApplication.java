package com.pds.tp.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ReportApplication(
        String reportingPlayerUsername,
        @JsonAlias("lobbyId") String scrimId,
        String reportedPlayerUsername,
        String reason
) {
}


