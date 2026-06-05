package com.pds.tp.domain.state;

import com.pds.tp.domain.entity.Player;
import com.pds.tp.domain.shared.RankScale;

public class SearchingState implements ScrimState {
    @Override
    public void postular(ScrimContext ctx, Player player, String role) {
        if (ctx.getLobby().getPlayers().contains(player)) {
            throw StateErrorStyle.invalidTransition("Player is already in the lobby.");
        }
        if (ctx.getLobby().getPlayers().size() >= ctx.getLobby().getMaxPlayers()) {
            throw StateErrorStyle.invalidTransition("Lobby is already full.");
        }
        if (!player.getRegion().equalsIgnoreCase(ctx.getLobby().getRegion())) {
            throw StateErrorStyle.invalidTransition("Player region does not match scrim region.");
        }

        int playerRank = RankScale.toValue(player.getVisibleRank());
        int minRank = RankScale.toValue(ctx.getLobby().getMinRank());
        int maxRank = RankScale.toValue(ctx.getLobby().getMaxRank());
        if (playerRank < minRank || playerRank > maxRank) {
            throw StateErrorStyle.invalidTransition("Player rank is outside scrim rank limits.");
        }

        ctx.getLobby().getPlayers().add(player);

        if (ctx.getLobby().getPlayers().size() == ctx.getLobby().getMaxPlayers()) {
            ctx.setState(new CreatedLobbyState());
        }
    }

    @Override
    public void confirmar(ScrimContext ctx, Player player) {
        throw StateErrorStyle.invalidTransition("Cannot confirm while searching for players.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw StateErrorStyle.invalidTransition("Cannot start while searching for players.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw StateErrorStyle.invalidTransition("Cannot finish while searching for players.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        ctx.setState(new CanceledState());
    }

    @Override
    public String getStatusName() {
        return "Buscando";
    }
}

