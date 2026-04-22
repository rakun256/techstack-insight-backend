    package com.emreuslu.techstack.backend.ingestion.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.emreuslu.techstack.backend.ingestion.dto.IngestionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IngestionSchedulerServiceTest {

    private IngestionProperties ingestionProperties;
    private JobIngestionFacade jobIngestionFacade;
    private IngestionSchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        ingestionProperties = new IngestionProperties();
        jobIngestionFacade = mock(JobIngestionFacade.class);
        schedulerService = new IngestionSchedulerService(ingestionProperties, jobIngestionFacade);

        IngestionProperties.Source first = new IngestionProperties.Source();
        first.setType("GREENHOUSE");
        first.setToken("vercel");

        IngestionProperties.Source second = new IngestionProperties.Source();
        second.setType("LEVER");
        second.setToken("plaid");

        ingestionProperties.getSources().add(first);
        ingestionProperties.getSources().add(second);
    }

    @Test
    void continuesProcessingSourcesWhenOneSourceFails() {
        doThrow(new IllegalStateException("parse failed"))
                .when(jobIngestionFacade)
                .ingestConfiguredSource("GREENHOUSE", "vercel");

        schedulerService.runScheduledIngestion();

        verify(jobIngestionFacade, times(1)).ingestConfiguredSource("GREENHOUSE", "vercel");
        verify(jobIngestionFacade, times(1)).ingestConfiguredSource("LEVER", "plaid");
    }
}

