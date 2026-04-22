package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.dto.IngestionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Scheduled(
            fixedDelayString = "${ingestion.scheduler.fixed-delay-ms:3600000}",
            initialDelayString = "${ingestion.scheduler.initial-delay-ms:30000}"
    )
    public void runScheduledIngestion() {
        if (!ingestionProperties.getScheduler().isEnabled()) {
            return;
        }

        for (IngestionProperties.Source source : ingestionProperties.getSources()) {
            if (!source.isEnabled()) {
                continue;
            }
            try {
                jobIngestionFacade.ingestConfiguredSource(source.getType(), source.getToken());
            } catch (Exception exception) {
                log.error(
                        "scheduled_ingestion_source_failed source={} token={} reason={}",
                        source.getType(),
                        source.getToken(),
                        exception.getMessage(),
                        exception
                );
            }
        }
    }
}

