package com.pds.tp.domain.state;

import com.pds.tp.domain.entity.Player;

/**
 * Base class for terminal states where lifecycle operations are no longer allowed.
 */
public abstract class AbstractTerminalState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Player player, String role) {
        throw StateErrorStyle.invalidTransition(defaultTerminalMessage());
    }

    @Override
    public void confirmar(ScrimContext ctx, Player player) {
        throw StateErrorStyle.invalidTransition(defaultTerminalMessage());
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw StateErrorStyle.invalidTransition(defaultTerminalMessage());
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw StateErrorStyle.invalidTransition(finalizeMessage());
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        throw StateErrorStyle.invalidTransition(cancelMessage());
    }

    protected String defaultTerminalMessage() {
        return getStatusName() + ".";
    }

    protected String finalizeMessage() {
        return defaultTerminalMessage();
    }

    protected String cancelMessage() {
        return defaultTerminalMessage();
    }
}
