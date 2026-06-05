package com.pds.tp.domain.state;

public class CanceledState extends AbstractTerminalState {
    @Override
    protected String defaultTerminalMessage() {
        return "Scrim is canceled.";
    }

    @Override
    protected String cancelMessage() {
        return "Scrim is already canceled.";
    }

    @Override
    public String getStatusName() {
        return "Cancelado";
    }
}

