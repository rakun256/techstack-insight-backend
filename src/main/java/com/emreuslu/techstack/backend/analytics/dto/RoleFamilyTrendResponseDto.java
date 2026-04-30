package com.emreuslu.techstack.backend.analytics.dto;

import java.time.LocalDate;

public record RoleFamilyTrendResponseDto(
        LocalDate date,
        String roleFamily,
        Long jobCount
) {
}

