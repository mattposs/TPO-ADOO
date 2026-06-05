package com.pds.tp.domain.strategy;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ByHistoryStrategy implements MatchmakingStrategy {
    @Override
    public List<Player> seleccionar(List<Player> candidatos, Lobby lobby) {
        return candidatos.stream()
                .filter(player -> player.getRegion().equalsIgnoreCase(lobby.getRegion()))
                .sorted(Comparator
                        .comparingInt(Player::getWins).reversed()
                        .thenComparingInt(Player::getGamesPlayed))
                .limit(Math.max(0, lobby.getMaxPlayers() - lobby.getPlayers().size()))
                .toList();
    }
}

