package com.pds.tp.domain.strategy;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ByLatencyStrategy implements MatchmakingStrategy {
    @Override
    public List<Player> seleccionar(List<Player> candidatos, Lobby lobby) {
        int remainingSlots = Math.max(0, remainingSlots(lobby));

        return candidatos.stream()
                .filter(p -> p.getRegion().equalsIgnoreCase(lobby.getRegion()))
                .filter(p -> p.getAveragePingMs() <= lobby.getMaxPing())
                .limit(remainingSlots)
                .collect(Collectors.toList());
    }
}

