package com.pds.tp.domain.state;

public class FinishedState extends AbstractTerminalState {
    @Override
    protected String defaultTerminalMessage() {
        return "Scrim is already finished.";
    }

    @Override
    protected String finalizeMessage() {
        return "Scrim is already finished.";
    }

    @Override
    public String getStatusName() {
        return "Finalizado";
    }
}

