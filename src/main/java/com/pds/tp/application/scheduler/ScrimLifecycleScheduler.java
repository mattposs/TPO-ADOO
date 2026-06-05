package com.pds.tp.application.scheduler;

import com.pds.tp.application.service.ScrimService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ScrimLifecycleScheduler {
    private static final long AUTO_FINALIZE_HOURS = 4L;

    private final ScrimService scrimService;

    public ScrimLifecycleScheduler(ScrimService scrimService) {
        this.scrimService = scrimService;
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void progressScrimLifecycle() {
        LocalDateTime now = LocalDateTime.now();

        int started = scrimService.autoStartConfirmedLobbies(now);
        int finalized = scrimService.autoFinalizeRunningScrims(now, AUTO_FINALIZE_HOURS);

        if (started > 0 || finalized > 0) {
            log.info("Scheduler lifecycle update -> started: {}, finalized: {}", started, finalized);
        }
    }
}

