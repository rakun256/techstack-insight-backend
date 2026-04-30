package com.emreuslu.techstack.backend.integration.lever.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.ingestion.service.SoftwareRoleClassificationService;
import com.emreuslu.techstack.backend.ingestion.service.TextNormalizationService;
import com.emreuslu.techstack.backend.integration.lever.dto.LeverJobResponseDto;
import com.emreuslu.techstack.backend.job.service.TitleAliasService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LeverJobMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LeverJobMapper mapper;

    @BeforeEach
    void setUp() {
        TitleAliasService titleAliasService = mock(TitleAliasService.class);
        mapper = new LeverJobMapper(
                new TextNormalizationService(),
                new SoftwareRoleClassificationService(titleAliasService)
        );
    }

    @Test
    void buildsAnalysisTextFromLeverStructuredFields() {
        LeverJobResponseDto dto = new LeverJobResponseDto(
                "job-1",
                "Machine Learning Engineer",
                "General description",
                "Work on ML systems",
                "Build pipelines",
                "You will own models",
                "https://example/hosted",
                "https://example/apply",
                1710000000L,
                1710000100L,
                new LeverJobResponseDto.CategoriesDto("Remote", "Engineering", "AI", "Full-time"),
                List.of(
                        new LeverJobResponseDto.ListSectionDto(
                                "Responsibilities",
                                TextNode.valueOf("Deploy models to production")
                        )
                )
        );

        NormalizedJobDto result = mapper.toNormalizedJob(dto, "plaid");

        assertThat(result.analysisText()).contains("Machine Learning Engineer");
        assertThat(result.analysisText()).contains("Responsibilities");
        assertThat(result.analysisText()).contains("Deploy models to production");
        assertThat(result.roleFamily()).isEqualTo("MACHINE_LEARNING_AI");
        assertThat(result.isSoftwareRelevant()).isTrue();
    }

    @Test
    void handlesStringArrayObjectAndNullListContent() throws Exception {
        LeverJobResponseDto dto = new LeverJobResponseDto(
                "job-3",
                "Backend Engineer",
                null,
                "Build distributed services",
                null,
                null,
                "https://example/hosted",
                "https://example/apply",
                1710000000L,
                1710000100L,
                new LeverJobResponseDto.CategoriesDto("Hybrid", "Engineering", "Platform", "Full-time"),
                List.of(
                        new LeverJobResponseDto.ListSectionDto("Stack", TextNode.valueOf("Java, Spring")),
                        new LeverJobResponseDto.ListSectionDto(
                                "Qualifications",
                                OBJECT_MAPPER.readTree("[\"Kubernetes\",{\"text\":\"AWS\"}]")
                        ),
                        new LeverJobResponseDto.ListSectionDto(
                                "Details",
                                OBJECT_MAPPER.readTree("{\"focus\":\"APIs\",\"level\":\"Senior\"}")
                        ),
                        new LeverJobResponseDto.ListSectionDto("Optional", null)
                )
        );

        NormalizedJobDto result = mapper.toNormalizedJob(dto, "plaid");

        assertThat(result.analysisText()).contains("Stack");
        assertThat(result.analysisText()).contains("Java, Spring");
        assertThat(result.analysisText()).contains("Kubernetes");
        assertThat(result.analysisText()).contains("AWS");
        assertThat(result.analysisText()).contains("focus");
        assertThat(result.analysisText()).contains("APIs");
        assertThat(result.isSoftwareRelevant()).isTrue();
    }

    @Test
    void marksSalesRoleAsNonSoftware() {
        LeverJobResponseDto dto = new LeverJobResponseDto(
                "job-2",
                "Account Executive",
                null,
                "Drive revenue growth",
                null,
                null,
                "https://example/hosted",
                "https://example/apply",
                1710000000L,
                1710000100L,
                new LeverJobResponseDto.CategoriesDto("Istanbul", "Sales", "Revenue", "Full-time"),
                List.of()
        );

        NormalizedJobDto result = mapper.toNormalizedJob(dto, "plaid");

        assertThat(result.isSoftwareRelevant()).isFalse();
        assertThat(result.roleFamily()).isEqualTo("SALES");
    }
}

