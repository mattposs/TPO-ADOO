package com.pds.tp.domain.strategy;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import com.pds.tp.domain.shared.RankScale;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
public class ByMMRStrategy implements MatchmakingStrategy {
    @Override
    public List<Player> seleccionar(List<Player> candidatos, Lobby lobby) {
        int minLobbyRank = RankScale.toValue(lobby.getMinRank());
        int maxLobbyRank = RankScale.toValue(lobby.getMaxRank());
        int remainingSlots = Math.max(0, remainingSlots(lobby));

        return candidatos.stream()
                .filter(p -> {
                    int playerRank = RankScale.toValue(p.getVisibleRank());
                    return playerRank >= minLobbyRank && playerRank <= maxLobbyRank;
                })
                .limit(remainingSlots)
                .collect(Collectors.toList());
    }
}

