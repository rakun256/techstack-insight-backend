package com.emreuslu.techstack.backend.ingestion.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.emreuslu.techstack.backend.ingestion.dto.RoleClassificationResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SoftwareRoleClassificationServiceTest {

    private SoftwareRoleClassificationService service;
    private TextNormalizationService textNormalizationService;

    @BeforeEach
    void setUp() {
        service = new SoftwareRoleClassificationService();
        textNormalizationService = new TextNormalizationService();
    }

    @Test
    void classifiesSoftwareRoleAsRelevant() {
        RoleClassificationResultDto result = service.classify(
                "Java Backend Engineer",
                "Engineering",
                "Platform",
                "Java Spring Boot PostgreSQL Kubernetes",
                textNormalizationService
        );

        assertThat(result.softwareRelevant()).isTrue();
        assertThat(result.roleFamily()).isEqualTo("BACKEND");
    }

    @Test
    void classifiesSalesRoleAsNonRelevant() {
        RoleClassificationResultDto result = service.classify(
                "Account Executive",
                "Sales",
                "Revenue",
                "Pipeline quota and outbound outreach",
                textNormalizationService
        );

        assertThat(result.softwareRelevant()).isFalse();
        assertThat(result.roleFamily()).isEqualTo("SALES");
    }

    @Test
    void normalizesTitleDuringClassification() {
        RoleClassificationResultDto result = service.classify(
                "  Senior   Software Engineer - Client SDK  ",
                "Engineering",
                null,
                "C++ SDK API",
                textNormalizationService
        );

        assertThat(result.normalizedTitle()).isEqualTo("Senior Software Engineer - Client SDK");
    }
}

