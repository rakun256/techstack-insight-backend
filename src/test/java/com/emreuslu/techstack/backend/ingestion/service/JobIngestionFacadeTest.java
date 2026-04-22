package com.emreuslu.techstack.backend.ingestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.emreuslu.techstack.backend.ingestion.dto.IngestionRunStatsDto;
import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.integration.greenhouse.client.GreenhouseClient;
import com.emreuslu.techstack.backend.integration.greenhouse.dto.GreenhouseJobResponseDto;
import com.emreuslu.techstack.backend.integration.greenhouse.mapper.GreenhouseJobMapper;
import com.emreuslu.techstack.backend.integration.lever.client.LeverClient;
import com.emreuslu.techstack.backend.integration.lever.mapper.LeverJobMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JobIngestionFacadeTest {

    private IngestionService ingestionService;
    private GreenhouseClient greenhouseClient;
    private GreenhouseJobMapper greenhouseJobMapper;
    private LeverClient leverClient;
    private LeverJobMapper leverJobMapper;
    private JobIngestionFacade facade;

    @BeforeEach
    void setUp() {
        ingestionService = Mockito.mock(IngestionService.class);
        greenhouseClient = Mockito.mock(GreenhouseClient.class);
        greenhouseJobMapper = Mockito.mock(GreenhouseJobMapper.class);
        leverClient = Mockito.mock(LeverClient.class);
        leverJobMapper = Mockito.mock(LeverJobMapper.class);

        facade = new JobIngestionFacade(
                ingestionService,
                greenhouseClient,
                greenhouseJobMapper,
                leverClient,
                leverJobMapper
        );
    }

    @Test
    void preventsConcurrentRunForSameSourceToken() throws Exception {
        CountDownLatch firstCallEntered = new CountDownLatch(1);
        CountDownLatch releaseFirstCall = new CountDownLatch(1);

        when(greenhouseClient.fetchJobs("vercel")).thenAnswer(invocation -> {
            firstCallEntered.countDown();
            releaseFirstCall.await(2, TimeUnit.SECONDS);
            return List.<GreenhouseJobResponseDto>of();
        });

        List<NormalizedJobDto> normalizedJobs = List.of(
                new NormalizedJobDto(
                        "GREENHOUSE",
                        "job-1",
                        null,
                        "Vercel",
                        "Software Engineer",
                        "Software Engineer",
                        "SOFTWARE_ENGINEERING_GENERAL",
                        "GENERAL",
                        true,
                        70,
                        "signal",
                        "Remote",
                        "Remote",
                        null,
                        true,
                        false,
                        "desc",
                        "desc",
                        "https://example",
                        LocalDate.now(),
                        "Engineering",
                        null,
                        "fp",
                        "dk",
                        null
                )
        );

        when(greenhouseJobMapper.toNormalizedJobs(any(), any())).thenReturn(normalizedJobs);
        when(ingestionService.ingestAll(any(), any(), any())).thenReturn(
                new IngestionRunStatsDto("GREENHOUSE", "vercel", 1, 1, 0, 1, 2, 0, 0, 0L, "SUCCESS")
        );

        CompletableFuture<IngestionRunStatsDto> firstRun = CompletableFuture.supplyAsync(
                () -> facade.ingestConfiguredSource("GREENHOUSE", "vercel")
        );

        assertThat(firstCallEntered.await(1, TimeUnit.SECONDS)).isTrue();

        IngestionRunStatsDto secondRun = facade.ingestConfiguredSource("GREENHOUSE", "vercel");
        assertThat(secondRun.status()).isEqualTo("SKIPPED_ALREADY_RUNNING");

        releaseFirstCall.countDown();
        IngestionRunStatsDto firstResult = firstRun.get(2, TimeUnit.SECONDS);
        assertThat(firstResult.status()).isEqualTo("SUCCESS");
    }
}

