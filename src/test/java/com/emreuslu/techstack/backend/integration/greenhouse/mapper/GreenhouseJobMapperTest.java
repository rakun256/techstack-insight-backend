package com.emreuslu.techstack.backend.integration.greenhouse.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.ingestion.service.SoftwareRoleClassificationService;
import com.emreuslu.techstack.backend.ingestion.service.TextNormalizationService;
import com.emreuslu.techstack.backend.integration.greenhouse.dto.GreenhouseJobResponseDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GreenhouseJobMapperTest {

    private GreenhouseJobMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GreenhouseJobMapper(new TextNormalizationService(), new SoftwareRoleClassificationService());
    }

    @Test
    void buildsRichAnalysisTextFromGreenhousePayload() {
        GreenhouseJobResponseDto dto = new GreenhouseJobResponseDto(
                42L,
                "Software Engineer, Deployment Infrastructure",
                new GreenhouseJobResponseDto.LocationDto("Remote - US"),
                "https://example/apply",
                "2026-01-10T00:00:00Z",
                "2026-01-12T00:00:00Z",
                "<p>Build platform services with Kubernetes and AWS</p>",
                "Vercel",
                List.of(new GreenhouseJobResponseDto.DepartmentDto("Engineering")),
                List.of(new GreenhouseJobResponseDto.OfficeDto("United States")),
                List.of(new GreenhouseJobResponseDto.MetadataDto("Level", "Senior"))
        );

        NormalizedJobDto result = mapper.toNormalizedJob(dto, "vercel");

        assertThat(result.analysisText()).contains("Software Engineer, Deployment Infrastructure");
        assertThat(result.analysisText()).contains("Build platform services with Kubernetes and AWS");
        assertThat(result.departmentRaw()).contains("Engineering");
        assertThat(result.isSoftwareRelevant()).isTrue();
    }
}

