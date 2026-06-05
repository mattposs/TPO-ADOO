package com.pds.tp.application.service;

import com.pds.tp.application.dto.LobbyApplication;
import com.pds.tp.application.dto.LobbyConfirmation;
import com.pds.tp.application.dto.ScrimData;
import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import com.pds.tp.domain.entity.Scrim;
import com.pds.tp.domain.entity.ScrimStatistics;
import com.pds.tp.domain.state.ScrimStateResolver;
import com.pds.tp.domain.strategy.ByMMRStrategy;
import com.pds.tp.infrastructure.repository.LobbyRepository;
import com.pds.tp.infrastructure.repository.PlayerRepository;
import com.pds.tp.infrastructure.repository.ScrimRepository;
import com.pds.tp.infrastructure.repository.ScrimStatisticsRepository;
import com.pds.tp.infrastructure.repository.WaitlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.pds.tp.support.TestFixtures.lobby;
import static com.pds.tp.support.TestFixtures.player;
import static com.pds.tp.support.TestFixtures.setId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrimServiceFlowIntegrationTest {

    @Mock
    private ScrimRepository scrimRepository;

    @Mock
    private LobbyRepository lobbyRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ScrimStatisticsRepository scrimStatisticsRepository;

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ScrimService scrimService;

    @BeforeEach
    void setUp() {
        scrimService = new ScrimService(
                scrimRepository,
                lobbyRepository,
                playerRepository,
                scrimStatisticsRepository,
                waitlistRepository,
                eventPublisher,
                new ByMMRStrategy(),
                new ScrimStateResolver()
        );
    }

    @Test
    void shouldExecuteCoreFlowApplyConfirmStartAndFinish() {
        Player host = player("host", "host@test.com", "LAS");
        Player candidate = player("candidate", "candidate@test.com", "LAS");
        host.setVisibleRank("PLATA");
        candidate.setVisibleRank("PLATA");

        Lobby scrimLobby = lobby(host, 1, 2, "Buscando", "BRONCE", "DIAMANTE", "HAVEN", List.of(host));
        UUID lobbyId = UUID.randomUUID();
        setId(scrimLobby, lobbyId);

        when(lobbyRepository.getReferenceById(lobbyId)).thenReturn(scrimLobby);
        when(lobbyRepository.save(any(Lobby.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(playerRepository.findByUsername("candidate")).thenReturn(candidate);
        when(playerRepository.findByUsername("host")).thenReturn(host);

        LobbyConfirmation applyResult = scrimService.applyToLobby(new LobbyApplication("candidate", lobbyId.toString(), "FLEX"));

        assertEquals("Confirmed", applyResult.status());
        assertEquals("LobbyArmado", scrimLobby.getStatus());
        assertEquals(2, scrimLobby.getPlayers().size());

        String confirmHost = scrimService.confirmParticipation(lobbyId, "host");
        String confirmCandidate = scrimService.confirmParticipation(lobbyId, "candidate");

        assertEquals("Player confirmed successfully.", confirmHost);
        assertEquals("Player confirmed successfully.", confirmCandidate);
        assertEquals("Confirmado", scrimLobby.getStatus());

        when(scrimRepository.findByLobbyId(scrimLobby)).thenReturn(Optional.empty());
        when(scrimRepository.save(any(Scrim.class))).thenAnswer(invocation -> {
            Scrim scrim = invocation.getArgument(0);
            if (scrim.getId() == null) {
                setId(scrim, UUID.randomUUID());
            }
            return scrim;
        });
        when(scrimStatisticsRepository.save(any(ScrimStatistics.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Scrim scrim = scrimService.startScrim(new ScrimData(lobbyId.toString()));

        assertEquals("EnJuego", scrimLobby.getStatus());
        assertEquals("EnJuego", scrim.getStatus());

        ScrimStatistics stats = new ScrimStatistics(
                scrim,
                new ArrayList<>(scrimLobby.getPlayers().subList(0, 1)),
                new ArrayList<>(scrimLobby.getPlayers().subList(1, 2))
        );

        when(scrimRepository.getReferenceById(scrim.getId())).thenReturn(scrim);
        when(scrimStatisticsRepository.findByScrimId(scrim)).thenReturn(stats);

        String finishResult = scrimService.finishScrimById(scrim.getId());

        assertTrue(finishResult.contains("Scrim finished"));
        assertEquals("Finalizado", scrimLobby.getStatus());
        assertEquals("Finalizado", scrim.getStatus());
    }
}
