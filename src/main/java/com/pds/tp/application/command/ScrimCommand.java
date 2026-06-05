package com.pds.tp.application.command;

import com.pds.tp.domain.state.ScrimContext;

/**
 * Command contract for pre-game lobby actions that can be undone.
 */
public interface ScrimCommand {
    void execute(ScrimContext ctx);

    void undo(ScrimContext ctx);
}
