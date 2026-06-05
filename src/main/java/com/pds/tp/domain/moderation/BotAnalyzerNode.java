package com.pds.tp.domain.moderation;

import com.pds.tp.domain.entity.Report;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BotAnalyzerNode extends ModerationNode {

    private final List<String> toxicKeywords = List.of("insulto", "troll", "flame", "toxic");

    @Override
    public void handle(Report report) {
        log.info("BotAnalyzerNode: Ejecutando NLP sobre el reporte...");

        boolean isToxic = toxicKeywords.stream().anyMatch(keyword ->
                report.getDescription() != null && report.getDescription().toLowerCase().contains(keyword));

        if (isToxic) {
            log.info("BotAnalyzerNode: Toxicidad detectada en el chat. Aplicando restricción de chat.");
            // En la vida real: db.updateStatus("RESUELTO_BOT");
        } else {
            log.info("BotAnalyzerNode: Contexto complejo. Escalando a moderación humana...");
            passToNext(report);
        }
    }
}

