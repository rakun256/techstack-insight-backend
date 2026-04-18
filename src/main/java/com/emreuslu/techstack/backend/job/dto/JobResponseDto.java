package com.emreuslu.techstack.backend.job.dto;

import java.time.LocalDate;
import java.util.UUID;

public record JobResponseDto(
        Long id,
        String externalId,
        String source,
        String title,
        String location,
        String description,
        String applyUrl,
        LocalDate postedAt,
        UUID companyId,
        String companyName
) {
}

