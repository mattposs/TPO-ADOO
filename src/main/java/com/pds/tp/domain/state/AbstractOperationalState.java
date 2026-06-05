package com.pds.tp.domain.state;

import com.pds.tp.domain.entity.Player;

/**
 * Base class for non-terminal states where only specific transitions are allowed.
 * Unsupported operations fail fast with a consistent message format.
 */
public abstract class AbstractOperationalState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Player player, String role) {
        throw StateErrorStyle.invalidTransition(postularRejectedMessage());
    }

    @Override
    public void confirmar(ScrimContext ctx, Player player) {
        throw StateErrorStyle.invalidTransition(confirmarRejectedMessage());
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw StateErrorStyle.invalidTransition(iniciarRejectedMessage());
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw StateErrorStyle.invalidTransition(finalizarRejectedMessage());
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        throw StateErrorStyle.invalidTransition(cancelarRejectedMessage());
    }

    protected String postularRejectedMessage() {
        return operationRejectedMessage();
    }

    protected String confirmarRejectedMessage() {
        return operationRejectedMessage();
    }

    protected String iniciarRejectedMessage() {
        return operationRejectedMessage();
    }

    protected String finalizarRejectedMessage() {
        return operationRejectedMessage();
    }

    protected String cancelarRejectedMessage() {
        return operationRejectedMessage();
    }

    private String operationRejectedMessage() {
        return StateErrorStyle.genericRejectedOperation(getStatusName());
    }
}
