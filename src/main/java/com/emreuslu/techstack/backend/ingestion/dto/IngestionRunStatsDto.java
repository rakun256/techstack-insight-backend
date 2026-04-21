package com.emreuslu.techstack.backend.ingestion.dto;

public record IngestionRunStatsDto(
        String source,
        String token,
        int fetchedCount,
        int insertedCount,
        int skippedCount,
        int softwareRelevantCount,
        int extractedSkillsCount,
        int failureCount
) {

    public static IngestionRunStatsDto empty(String source, String token) {
        return new IngestionRunStatsDto(source, token, 0, 0, 0, 0, 0, 0);
    }
}

