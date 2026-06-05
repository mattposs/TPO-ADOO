package com.pds.tp.domain.state;

public class PlayingState extends AbstractOperationalState {
    @Override
    protected String postularRejectedMessage() {
        return "Scrim is in progress.";
    }

    @Override
    protected String confirmarRejectedMessage() {
        return "Scrim is in progress.";
    }

    @Override
    protected String iniciarRejectedMessage() {
        return "Scrim has already started.";
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        ctx.setState(new FinishedState());
    }

    @Override
    protected String cancelarRejectedMessage() {
        return "Cannot cancel a scrim that is already in progress; it must be finished.";
    }

    @Override
    public String getStatusName() {
        return "EnJuego";
    }
}

