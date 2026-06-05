package com.pds.tp.domain.state;

import com.pds.tp.domain.entity.Player;

public class CreatedLobbyState implements ScrimState {
    @Override
    public void postular(ScrimContext ctx, Player player, String role) {
        throw StateErrorStyle.invalidTransition("Lobby capacity is already full.");
    }

    @Override
    public void confirmar(ScrimContext ctx, Player player) {
        if (!ctx.getLobby().getPlayers().contains(player)) {
            throw StateErrorStyle.invalidTransition("Player does not belong to this lobby.");
        }
        boolean isNewConfirmation = ctx.getLobby().getConfirmedPlayerUsernames().add(player.getUsername());
        if (!isNewConfirmation) {
            throw StateErrorStyle.invalidTransition("Player has already confirmed participation.");
        }

        if (ctx.getLobby().getConfirmedPlayerUsernames().size() == ctx.getLobby().getMaxPlayers()) {
            ctx.setState(new ConfirmedState());
        }
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw StateErrorStyle.invalidTransition("Missing player confirmations.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw StateErrorStyle.invalidTransition("Scrim has not started yet.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        ctx.setState(new CanceledState());
    }

    @Override
    public String getStatusName() {
        return "LobbyArmado";
    }
}

