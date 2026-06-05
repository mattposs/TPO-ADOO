package com.pds.tp.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Event published when a new lobby/scrim is created.
 */
@Getter
public class ScrimCreatedEvent extends ApplicationEvent {
    private final UUID lobbyId;
    private final String game;
    private final String region;

    public ScrimCreatedEvent(Object source, UUID lobbyId, String game, String region) {
        super(source);
        this.lobbyId = lobbyId;
        this.game = game;
        this.region = region;
    }
}
