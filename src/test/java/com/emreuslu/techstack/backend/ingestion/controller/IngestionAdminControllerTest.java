package com.emreuslu.techstack.backend.ingestion.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emreuslu.techstack.backend.ingestion.dto.IngestionProperties;
import com.emreuslu.techstack.backend.ingestion.dto.IngestionRunStatsDto;
import com.emreuslu.techstack.backend.ingestion.service.JobIngestionFacade;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IngestionAdminControllerTest {

    private JobIngestionFacade jobIngestionFacade;
    private IngestionProperties ingestionProperties;
    private IngestionAdminController ingestionAdminController;

    @BeforeEach
    void setUp() {
        jobIngestionFacade = mock(JobIngestionFacade.class);
        ingestionProperties = new IngestionProperties();

        IngestionProperties.Source source = new IngestionProperties.Source();
        source.setType("GREENHOUSE");
        source.setToken("vercel");
        ingestionProperties.getSources().add(source);

        ingestionAdminController = new IngestionAdminController(jobIngestionFacade, ingestionProperties);
    }

    @Test
    void runAllEnabledSourcesUsesManualTrigger() {
        List<IngestionRunStatsDto> expected = List.of(
                new IngestionRunStatsDto("GREENHOUSE", "vercel", 1, 1, 0, 1, 2, 0, 0, 100L, "SUCCESS")
        );
        when(jobIngestionFacade.ingestAllConfiguredSources(ingestionProperties.getSources(), JobIngestionFacade.TRIGGER_MANUAL))
                .thenReturn(expected);

        List<IngestionRunStatsDto> body = ingestionAdminController.runAllEnabledSources().getBody();

        assertThat(body).isEqualTo(expected);
        verify(jobIngestionFacade, times(1)).ingestAllConfiguredSources(
                ingestionProperties.getSources(),
                JobIngestionFacade.TRIGGER_MANUAL
        );
    }

    @Test
    void runOneSourceUsesManualTrigger() {
        IngestionRunStatsDto expected = new IngestionRunStatsDto(
                "GREENHOUSE",
                "vercel",
                1,
                1,
                0,
                1,
                2,
                0,
                0,
                80L,
                "SUCCESS"
        );
        when(jobIngestionFacade.ingestConfiguredSource("GREENHOUSE", "vercel", JobIngestionFacade.TRIGGER_MANUAL))
                .thenReturn(expected);

        IngestionRunStatsDto body = ingestionAdminController.runOneSource("GREENHOUSE", "vercel").getBody();

        assertThat(body).isEqualTo(expected);
        verify(jobIngestionFacade, times(1)).ingestConfiguredSource(
                "GREENHOUSE",
                "vercel",
                JobIngestionFacade.TRIGGER_MANUAL
        );
    }
}

