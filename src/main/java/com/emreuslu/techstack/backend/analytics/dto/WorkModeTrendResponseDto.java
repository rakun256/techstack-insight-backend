package com.emreuslu.techstack.backend.analytics.dto;

import java.time.LocalDate;

public record WorkModeTrendResponseDto(
        LocalDate date,
        String workMode,
        Long jobCount
) {
}

