package com.pds.tp.domain.state;

import org.springframework.stereotype.Component;

/**
 * Maps persisted lobby status strings to concrete ScrimState implementations.
 */
@Component
public class ScrimStateResolver {
    private static final String STATUS_BUSCANDO = "Buscando";
    private static final String STATUS_LOBBY_ARMADO = "LobbyArmado";
    private static final String STATUS_CONFIRMADO = "Confirmado";
    private static final String STATUS_EN_JUEGO = "EnJuego";
    private static final String STATUS_FINALIZADO = "Finalizado";
    private static final String STATUS_CANCELADO = "Cancelado";

    public ScrimState resolve(String status) {
        if (status == null) {
            return new SearchingState();
        }

        return switch (status) {
            case STATUS_BUSCANDO -> new SearchingState();
            case STATUS_LOBBY_ARMADO -> new CreatedLobbyState();
            case STATUS_CONFIRMADO -> new ConfirmedState();
            case STATUS_EN_JUEGO -> new PlayingState();
            case STATUS_FINALIZADO -> new FinishedState();
            case STATUS_CANCELADO -> new CanceledState();
            default -> new SearchingState();
        };
    }
}
