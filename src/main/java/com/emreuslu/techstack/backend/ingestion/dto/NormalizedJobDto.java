package com.emreuslu.techstack.backend.ingestion.dto;

import java.time.LocalDate;

public record NormalizedJobDto(
        String externalId,
        String source,
        String companyExternalId,
        String companyName,
        String title,
        String location,
        String description,
        String applyUrl,
        LocalDate postedAt,
        String rawMetadata
) {
}

