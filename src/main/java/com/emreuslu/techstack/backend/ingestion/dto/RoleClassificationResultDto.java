package com.emreuslu.techstack.backend.ingestion.dto;

public record RoleClassificationResultDto(
        String normalizedTitle,
        String roleFamily,
        String roleSubfamily,
        boolean softwareRelevant,
        int relevanceScore,
        String relevanceReason
) {
}

