package com.emreuslu.techstack.backend.ingestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.emreuslu.techstack.backend.ingestion.dto.IngestionItemResultDto;
import com.emreuslu.techstack.backend.ingestion.dto.IngestionRunStatsDto;
import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IngestionServiceTest {

    private IngestionItemPersistenceService ingestionItemPersistenceService;
    private IngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionItemPersistenceService = Mockito.mock(IngestionItemPersistenceService.class);
        ingestionService = new IngestionService(ingestionItemPersistenceService);
    }

    @Test
    void skipsPersistenceForNonSoftwareRole() {
        NormalizedJobDto dto = new NormalizedJobDto(
                "LEVER",
                "job-100",
                null,
                "Example Co",
                "Account Executive",
                "Account Executive",
                "SALES",
                "NON_SOFTWARE",
                false,
                10,
                "non-tech keyword match",
                "Istanbul",
                "Istanbul",
                null,
                false,
                false,
                "Drive sales pipeline",
                "Drive sales pipeline",
                "https://example/apply",
                LocalDate.now(),
                "Sales",
                "Revenue",
                "LEVER:plaid:job-100",
                "LEVER:job-100",
                null
        );

        when(ingestionItemPersistenceService.persistOne(dto)).thenReturn(IngestionItemResultDto.skipped(false));

        IngestionService.IngestionOutcome outcome = ingestionService.ingestOne(dto);

        assertThat(outcome.inserted()).isFalse();
        assertThat(outcome.softwareRelevant()).isFalse();
    }

    @Test
    void continuesBatchWhenOneItemFailsAndCountsRemainAccurate() {
        NormalizedJobDto first = new NormalizedJobDto(
                "LEVER",
                "job-200",
                "example-co",
                "Example Co",
                "Java Backend Engineer",
                "Java Backend Engineer",
                "BACKEND",
                "GENERAL",
                true,
                82,
                "software role keywords and technical signals",
                "Remote - TR",
                "Remote - TR",
                "TR",
                true,
                false,
                "Build APIs with Java and Spring Boot",
                "Java Backend Engineer Build APIs with Java and Spring Boot",
                "https://example/apply",
                LocalDate.now(),
                "Engineering",
                "Platform",
                "LEVER:plaid:job-200",
                "LEVER:job-200",
                null
        );

        NormalizedJobDto second = new NormalizedJobDto(
                "LEVER",
                "job-201",
                "example-co",
                "Example Co",
                "Senior Software Engineer",
                "Senior Software Engineer",
                "SOFTWARE_ENGINEERING_GENERAL",
                "GENERAL",
                true,
                70,
                "software role keywords and technical signals",
                "Remote - TR",
                "Remote - TR",
                "TR",
                true,
                false,
                "Build APIs",
                "Build APIs",
                "https://example/apply",
                LocalDate.now(),
                "Engineering",
                "Platform",
                "LEVER:plaid:job-201",
                "LEVER:job-201",
                null
        );

        when(ingestionItemPersistenceService.persistOne(first))
                .thenReturn(IngestionItemResultDto.inserted(3, true));
        when(ingestionItemPersistenceService.persistOne(second))
                .thenThrow(new IllegalStateException("db write failed"));

        IngestionRunStatsDto stats = ingestionService.ingestAll(List.of(first, second), "LEVER", "plaid");

        assertThat(stats.fetchedCount()).isEqualTo(2);
        assertThat(stats.insertedCount()).isEqualTo(1);
        assertThat(stats.skippedCount()).isEqualTo(0);
        assertThat(stats.failureCount()).isEqualTo(1);
        assertThat(stats.extractedSkillsCount()).isEqualTo(3);
        assertThat(stats.companyReusedAfterDuplicateCount()).isEqualTo(1);
        assertThat(stats.status()).isEqualTo("PARTIAL_SUCCESS");
    }
}

