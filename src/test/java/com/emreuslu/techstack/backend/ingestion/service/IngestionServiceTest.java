package com.emreuslu.techstack.backend.ingestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emreuslu.techstack.backend.company.entity.Company;
import com.emreuslu.techstack.backend.company.repository.CompanyRepository;
import com.emreuslu.techstack.backend.extraction.service.SkillExtractionService;
import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import com.emreuslu.techstack.backend.jobskill.service.JobSkillService;
import com.emreuslu.techstack.backend.skill.service.SkillService;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

    @Test
    void persistsSoftwareAnalyticsFieldsForRelevantRole() {
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder()
                .id(companyId)
                .name("Example Co")
                .externalSource("LEVER")
                .externalCompanyId("example-co")
                .build();

        NormalizedJobDto dto = new NormalizedJobDto(
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

        when(jobRepository.findByExternalIdAndSource("job-200", "LEVER")).thenReturn(Optional.empty());
        when(companyRepository.findByExternalSourceAndExternalCompanyId("LEVER", "example-co")).thenReturn(Optional.of(company));
        when(jobRepository.save(Mockito.any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(skillExtractionService.extractSkills(Mockito.any())).thenReturn(java.util.List.of());

        IngestionService.IngestionOutcome outcome = ingestionService.ingestOne(dto);

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(jobCaptor.capture());
        Job savedJob = jobCaptor.getValue();

        assertThat(outcome.inserted()).isTrue();
        assertThat(savedJob.isSoftwareRelevant()).isTrue();
        assertThat(savedJob.getRoleFamily()).isEqualTo("BACKEND");
        assertThat(savedJob.getRoleSubfamily()).isEqualTo("GENERAL");
        assertThat(savedJob.getNormalizedTitle()).isEqualTo("Java Backend Engineer");
        assertThat(savedJob.getLocationNormalized()).isEqualTo("Remote - TR");
        assertThat(savedJob.getCountry()).isEqualTo("TR");
        assertThat(savedJob.isRemote()).isTrue();
        assertThat(savedJob.isHybrid()).isFalse();
    }
}

