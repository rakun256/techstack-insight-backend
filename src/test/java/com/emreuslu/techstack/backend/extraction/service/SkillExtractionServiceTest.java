package com.emreuslu.techstack.backend.extraction.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.emreuslu.techstack.backend.extraction.catalog.SkillKeywordCatalog;
import com.emreuslu.techstack.backend.extraction.dto.ExtractedSkillDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SkillExtractionServiceTest {

    private SkillExtractionService skillExtractionService;

    @BeforeEach
    void setUp() {
        skillExtractionService = new SkillExtractionService(new SkillKeywordCatalog());
    }

    @Test
    void returnsEmptyListForNullInput() {
        List<ExtractedSkillDto> result = skillExtractionService.extractSkills(null);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyListForBlankInput() {
        List<ExtractedSkillDto> result = skillExtractionService.extractSkills("   \n\t  ");

        assertThat(result).isEmpty();
    }

    @Test
    void extractsExpectedSkillsFromRealisticJobDescription() {
        String description = "We are looking for a backend engineer with Java and Spring Boot experience. "
                + "You will design REST API services, optimize PostgreSQL queries, and ship with Docker. "
                + "Daily workflow includes Git and AWS.";

        List<ExtractedSkillDto> result = skillExtractionService.extractSkills(description);

        assertThat(result)
                .extracting(ExtractedSkillDto::name)
                .containsExactly("Java", "Spring Boot", "PostgreSQL", "Docker", "Git", "REST API", "AWS");
    }

    @Test
    void doesNotReturnDuplicateSkillsForRepeatedKeywords() {
        String description = "Java Java java SPRING BOOT spring boot Java";

        List<ExtractedSkillDto> result = skillExtractionService.extractSkills(description);

        assertThat(result)
                .extracting(ExtractedSkillDto::name)
                .containsExactly("Java", "Spring Boot");
    }

    @Test
    void doesNotMatchJavaOnlyBecauseJavascriptAppears() {
        String description = "Frontend role focuses on JavaScript and React.";

        List<ExtractedSkillDto> result = skillExtractionService.extractSkills(description);

        assertThat(result)
                .extracting(ExtractedSkillDto::name)
                .contains("JavaScript", "React")
                .doesNotContain("Java");
    }

    @Test
    void matchesKeywordsCaseInsensitively() {
        String description = "Experience with pYtHoN and rEaCt is required.";

        List<ExtractedSkillDto> result = skillExtractionService.extractSkills(description);

        assertThat(result)
                .extracting(ExtractedSkillDto::name)
                .containsExactly("React", "Python");
    }

    @Test
    void returnsSingleResultWhenMultipleKeywordsMapToSameCanonicalSkill() {
        String description = "Looking for candidates with springboot and Spring Boot experience.";

        List<ExtractedSkillDto> result = skillExtractionService.extractSkills(description);

        assertThat(result)
                .filteredOn(skill -> skill.name().equals("Spring Boot"))
                .singleElement()
                .satisfies(skill -> assertThat(skill.matchedKeyword()).isEqualTo("spring boot"));
    }
}

