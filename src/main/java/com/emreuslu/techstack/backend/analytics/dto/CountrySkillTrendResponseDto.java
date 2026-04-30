package com.emreuslu.techstack.backend.analytics.dto;

import java.time.LocalDate;

public record CountrySkillTrendResponseDto(
        LocalDate date,
        String skillName,
        Long jobCount
) {
}

