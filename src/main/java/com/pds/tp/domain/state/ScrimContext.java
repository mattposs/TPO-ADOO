package com.pds.tp.domain.state;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import com.pds.tp.domain.event.ScrimStateChangedEvent;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;

/**
 * State pattern context for a Lobby: delegates actions to the current ScrimState
 * and keeps the persisted lobby status synchronized after each transition.
 */
@Getter
public class ScrimContext {
    private final Lobby lobby;
    private final ApplicationEventPublisher eventPublisher;
    private ScrimState state;

    public ScrimContext(Lobby lobby, ScrimState initialState, ApplicationEventPublisher eventPublisher) {
        this.lobby = lobby;
        this.state = initialState;
        this.eventPublisher = eventPublisher;
    }

    public void setState(ScrimState state) {
        this.state = state;
        this.lobby.setStatus(state.getStatusName());
        // Emit a domain event so subscribers can notify users on each status transition.
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new ScrimStateChangedEvent(this, lobby.getId(), state.getStatusName()));
        }
    }

    public void postular(Player player, String role) {
        state.postular(this, player, role);
    }

    public void confirmar(Player player) {
        state.confirmar(this, player);
    }

    public void iniciar() {
        state.iniciar(this);
    }

    public void finalizar() {
        state.finalizar(this);
    }

    public void cancelar() {
        state.cancelar(this);
    }
}
