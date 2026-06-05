package com.pds.tp.domain.strategy;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;

import java.util.List;

/**
 * Strategy abstraction for selecting players that can fill a lobby.
 */
public interface MatchmakingStrategy {
    List<Player> seleccionar(List<Player> candidatos, Lobby lobby);

    default int remainingSlots(Lobby lobby) {
        return lobby.getMaxPlayers() - lobby.getPlayers().size();
    }
}
