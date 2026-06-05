package com.pds.tp.application.service;

import com.pds.tp.application.dto.ReportApplication;
import com.pds.tp.application.dto.ReportConfirmation;
import com.pds.tp.domain.entity.Player;
import com.pds.tp.domain.entity.Report;
import com.pds.tp.domain.entity.Scrim;
import com.pds.tp.domain.moderation.AutoResolverNode;
import com.pds.tp.domain.moderation.BotAnalyzerNode;
import com.pds.tp.domain.moderation.HumanModNode;
import com.pds.tp.infrastructure.repository.PlayerRepository;
import com.pds.tp.infrastructure.repository.ReportRepository;
import com.pds.tp.infrastructure.repository.ScrimRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final PlayerRepository playerRepository;
    private final ScrimRepository scrimRepository;

    // Chain of Responsibility nodes.
    private final AutoResolverNode autoResolver;
    private final BotAnalyzerNode botAnalyzer;
    private final HumanModNode humanMod;

    public ReportService(ReportRepository reportRepository, PlayerRepository playerRepository,
                         ScrimRepository scrimRepository, AutoResolverNode autoResolver,
                         BotAnalyzerNode botAnalyzer, HumanModNode humanMod) {
        this.reportRepository = reportRepository;
        this.playerRepository = playerRepository;
        this.scrimRepository = scrimRepository;
        this.autoResolver = autoResolver;
        this.botAnalyzer = botAnalyzer;
        this.humanMod = humanMod;
    }

    @PostConstruct
    public void initChain() {
        // Wire moderation handlers in escalation order.
        autoResolver.setNext(botAnalyzer);
        botAnalyzer.setNext(humanMod);
    }

    public ReportConfirmation processReport(ReportApplication reportApp) {
        Player reportingPlayer = playerRepository.findByUsername(reportApp.reportingPlayerUsername());
        Player reportedPlayer = playerRepository.findByUsername(reportApp.reportedPlayerUsername());
        Scrim scrim = scrimRepository.getReferenceById(UUID.fromString(reportApp.scrimId()));

        if (!scrim.getStatus().equals("Finalizado")) {
            throw new IllegalStateException("You cannot report players from a non-finished scrim.");
        }

        Report report = new Report(scrim, reportingPlayer, reportedPlayer, reportApp.reason(), "Context Description Placeholder");
        report = reportRepository.save(report);

        // Execute moderation pipeline.
        autoResolver.handle(report);

        return new ReportConfirmation(report.getId(), report.getReportingPlayer().getUsername(),
                report.getScrimId().toString(), report.getReportedPlayer().getUsername(), report.getStatus());
    }
}

