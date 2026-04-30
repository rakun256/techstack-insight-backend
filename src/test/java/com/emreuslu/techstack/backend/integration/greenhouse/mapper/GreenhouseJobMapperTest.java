package com.emreuslu.techstack.backend.integration.greenhouse.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.ingestion.service.SoftwareRoleClassificationService;
import com.emreuslu.techstack.backend.ingestion.service.TextNormalizationService;
import com.emreuslu.techstack.backend.integration.greenhouse.dto.GreenhouseJobResponseDto;
import com.emreuslu.techstack.backend.job.service.TitleAliasService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GreenhouseJobMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GreenhouseJobMapper mapper;

    @BeforeEach
    void setUp() {
        TitleAliasService titleAliasService = mock(TitleAliasService.class);
        mapper = new GreenhouseJobMapper(
                new TextNormalizationService(),
                new SoftwareRoleClassificationService(titleAliasService)
        );
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
                List.of(new GreenhouseJobResponseDto.MetadataDto("Level", TextNode.valueOf("Senior")))
        );

        NormalizedJobDto result = mapper.toNormalizedJob(dto, "vercel");

        assertThat(result.analysisText()).contains("Software Engineer, Deployment Infrastructure");
        assertThat(result.analysisText()).contains("Build platform services with Kubernetes and AWS");
        assertThat(result.departmentRaw()).contains("Engineering");
        assertThat(result.isSoftwareRelevant()).isTrue();
    }

    @Test
    void handlesObjectAndArrayMetadataValuesWithoutFailing() throws Exception {
        GreenhouseJobResponseDto dto = new GreenhouseJobResponseDto(
                43L,
                "Backend Engineer",
                new GreenhouseJobResponseDto.LocationDto("Hybrid - Berlin"),
                "https://example/apply",
                "2026-01-10T00:00:00Z",
                "2026-01-12T00:00:00Z",
                "<p>Build APIs and data pipelines</p>",
                "Datadog",
                List.of(new GreenhouseJobResponseDto.DepartmentDto("Platform")),
                List.of(new GreenhouseJobResponseDto.OfficeDto("Germany")),
                List.of(
                        new GreenhouseJobResponseDto.MetadataDto(
                                "Tech Stack",
                                OBJECT_MAPPER.readTree("{\"primary\":\"Java\",\"secondary\":[\"Kotlin\",\"Spring\"]}")
                        ),
                        new GreenhouseJobResponseDto.MetadataDto(
                                "Focus",
                                OBJECT_MAPPER.readTree("[\"API\",\"Data\"]")
                        ),
                        new GreenhouseJobResponseDto.MetadataDto("Nullable", null)
                )
        );

        NormalizedJobDto result = mapper.toNormalizedJob(dto, "datadog");

        assertThat(result.analysisText()).contains("Tech Stack");
        assertThat(result.analysisText()).contains("primary");
        assertThat(result.analysisText()).contains("Java");
        assertThat(result.analysisText()).contains("API");
        assertThat(result.analysisText()).contains("Data");
        assertThat(result.isSoftwareRelevant()).isTrue();
    }
}

