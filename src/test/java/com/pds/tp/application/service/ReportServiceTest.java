package com.pds.tp.application.service;

import com.pds.tp.application.dto.ReportApplication;
import com.pds.tp.application.dto.ReportConfirmation;
import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import com.pds.tp.domain.entity.Report;
import com.pds.tp.domain.entity.Scrim;
import com.pds.tp.domain.moderation.AutoResolverNode;
import com.pds.tp.domain.moderation.BotAnalyzerNode;
import com.pds.tp.domain.moderation.HumanModNode;
import com.pds.tp.infrastructure.repository.PlayerRepository;
import com.pds.tp.infrastructure.repository.ReportRepository;
import com.pds.tp.infrastructure.repository.ScrimRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static com.pds.tp.support.TestFixtures.lobby;
import static com.pds.tp.support.TestFixtures.player;
import static com.pds.tp.support.TestFixtures.setId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ScrimRepository scrimRepository;

    @Mock
    private AutoResolverNode autoResolver;

    @Mock
    private BotAnalyzerNode botAnalyzer;

    @Mock
    private HumanModNode humanMod;

    @InjectMocks
    private ReportService reportService;

    @Test
    void shouldInitializeModerationChain() {
        reportService.initChain();

        verify(autoResolver).setNext(botAnalyzer);
        verify(botAnalyzer).setNext(humanMod);
    }

    @Test
    void shouldRejectReportsForNonFinishedScrims() {
        Player reporting = player("r1", "r1@test.com", "LAS");
        Player reported = player("r2", "r2@test.com", "LAS");
        Scrim scrim = buildScrim("EnJuego");

        when(playerRepository.findByUsername("r1")).thenReturn(reporting);
        when(playerRepository.findByUsername("r2")).thenReturn(reported);
        when(scrimRepository.getReferenceById(any(UUID.class))).thenReturn(scrim);

        ReportApplication app = new ReportApplication("r1", UUID.randomUUID().toString(), "r2", "AFK");

        assertThrows(IllegalStateException.class, () -> reportService.processReport(app));
    }

    @Test
    void shouldPersistAndReturnConfirmationForFinishedScrims() {
        Player reporting = player("r1", "r1@test.com", "LAS");
        Player reported = player("r2", "r2@test.com", "LAS");
        Scrim scrim = buildScrim("Finalizado");

        when(playerRepository.findByUsername("r1")).thenReturn(reporting);
        when(playerRepository.findByUsername("r2")).thenReturn(reported);
        when(scrimRepository.getReferenceById(any(UUID.class))).thenReturn(scrim);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            setId(report, UUID.randomUUID());
            return report;
        });

        ReportApplication app = new ReportApplication("r1", UUID.randomUUID().toString(), "r2", "AFK");

        ReportConfirmation confirmation = reportService.processReport(app);

        assertNotNull(confirmation.reportId());
        assertEquals("r1", confirmation.reportingPlayerUsername());
        assertEquals("r2", confirmation.reportedPlayerUsername());
        assertEquals("Created", confirmation.status());
        verify(autoResolver).handle(any(Report.class));
    }

    private Scrim buildScrim(String status) {
        Player host = player("host", "host@test.com", "LAS");
        Lobby scrimLobby = lobby(host, 1, 10, "Buscando", "BRONCE", "DIAMANTE", "HAVEN", List.of(host));
        return new Scrim(scrimLobby, "VALORANT", "HAVEN", status);
    }
}
