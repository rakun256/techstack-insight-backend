package com.emreuslu.techstack.backend.ingestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.emreuslu.techstack.backend.company.repository.CompanyRepository;
import com.emreuslu.techstack.backend.extraction.service.SkillExtractionService;
import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import com.emreuslu.techstack.backend.jobskill.service.JobSkillService;
import com.emreuslu.techstack.backend.skill.service.SkillService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IngestionServiceTest {

    private CompanyRepository companyRepository;
    private JobRepository jobRepository;
    private SkillExtractionService skillExtractionService;
    private SkillService skillService;
    private JobSkillService jobSkillService;
    private IngestionService ingestionService;

    @BeforeEach
    void setUp() {
        companyRepository = Mockito.mock(CompanyRepository.class);
        jobRepository = Mockito.mock(JobRepository.class);
        skillExtractionService = Mockito.mock(SkillExtractionService.class);
        skillService = Mockito.mock(SkillService.class);
        jobSkillService = Mockito.mock(JobSkillService.class);

        ingestionService = new IngestionService(
                companyRepository,
                jobRepository,
                skillExtractionService,
                skillService,
                jobSkillService
        );
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

        IngestionService.IngestionOutcome outcome = ingestionService.ingestOne(dto);

        assertThat(outcome.inserted()).isFalse();
        assertThat(outcome.softwareRelevant()).isFalse();
        verify(jobRepository, never()).save(Mockito.any());
        verify(companyRepository, never()).save(Mockito.any());
    }
}

