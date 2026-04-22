package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.dto.IngestionProperties;
import com.emreuslu.techstack.backend.ingestion.dto.IngestionRunStatsDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class IngestionSchedulerService {

    private final IngestionProperties ingestionProperties;
    private final JobIngestionFacade jobIngestionFacade;

    @EventListener(ApplicationReadyEvent.class)
    public void runStartupIngestion() {
        if (!ingestionProperties.isRunOnStartup()) {
            return;
        }

        log.info("ingestion_cycle_started trigger=STARTUP");
        List<IngestionRunStatsDto> results = jobIngestionFacade.ingestAllConfiguredSources(
                ingestionProperties.getSources(),
                JobIngestionFacade.TRIGGER_STARTUP
        );
        log.info("ingestion_cycle_finished trigger=STARTUP sourceCount={}", results.size());
    }

    @Scheduled(cron = "${app.ingestion.scheduler-cron:0 0 3 * * *}")
    public void runScheduledIngestion() {
        if (!ingestionProperties.isSchedulerEnabled()) {
            return;
        }

        log.info("ingestion_cycle_started trigger=SCHEDULED");
        List<IngestionRunStatsDto> results = jobIngestionFacade.ingestAllConfiguredSources(
                ingestionProperties.getSources(),
                JobIngestionFacade.TRIGGER_SCHEDULED
        );
        log.info("ingestion_cycle_finished trigger=SCHEDULED sourceCount={}", results.size());
    }
}

