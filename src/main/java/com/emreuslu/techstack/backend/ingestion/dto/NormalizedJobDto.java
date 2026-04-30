package com.emreuslu.techstack.backend.ingestion.dto;

import java.time.LocalDate;

public record NormalizedJobDto(
        String source,
        String externalJobId,
        String externalCompanyId,
        String companyName,
        String rawTitle,
        String normalizedTitle,
        String roleFamily,
        String roleSubfamily,
        boolean isSoftwareRelevant,
        int relevanceScore,
        String relevanceReason,
        String locationRaw,
        String locationNormalized,
        String country,
        boolean isRemote,
        boolean isHybrid,
        String descriptionPlainNormalized,
        String analysisText,
        String applyUrl,
        LocalDate postedAt,
        String departmentRaw,
        String teamRaw,
        String sourceFingerprint,
        String dedupeKey,
        String rawMetadata,
        String rawJobJson
) {
}

