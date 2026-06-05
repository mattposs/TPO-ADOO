package com.pds.tp.domain.state;

public class ConfirmedState extends AbstractOperationalState {
    @Override
    protected String postularRejectedMessage() {
        return "Lobby is full and already confirmed.";
    }

    @Override
    protected String confirmarRejectedMessage() {
        return "All players are already confirmed.";
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        ctx.setState(new PlayingState());
    }

    @Override
    protected String finalizarRejectedMessage() {
        return "Scrim must start before it can be finished.";
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        ctx.setState(new CanceledState());
    }

    @Override
    public String getStatusName() {
        return "Confirmado";
    }
}

