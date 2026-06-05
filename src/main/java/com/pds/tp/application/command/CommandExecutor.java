package com.pds.tp.application.command;

import com.pds.tp.domain.state.ScrimContext;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Executes commands and keeps an in-memory stack for one-step-at-a-time undo.
 */
@Component
public class CommandExecutor {
    private final Deque<ScrimCommand> history = new ArrayDeque<>();

    public void executeCommand(ScrimCommand command, ScrimContext context) {
        command.execute(context);
        history.push(command);
    }

    public void undoLastCommand(ScrimContext context) {
        if (!history.isEmpty()) {
            ScrimCommand lastCommand = history.pop();
            lastCommand.undo(context);
        }
    }
}
