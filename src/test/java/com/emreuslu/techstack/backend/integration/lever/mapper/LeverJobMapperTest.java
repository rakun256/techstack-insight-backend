package com.emreuslu.techstack.backend.integration.lever.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.ingestion.service.SoftwareRoleClassificationService;
import com.emreuslu.techstack.backend.ingestion.service.TextNormalizationService;
import com.emreuslu.techstack.backend.integration.lever.dto.LeverJobResponseDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LeverJobMapperTest {

    private LeverJobMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LeverJobMapper(new TextNormalizationService(), new SoftwareRoleClassificationService());
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
                                List.of(new LeverJobResponseDto.ListContentDto("Deploy models to production"))
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

