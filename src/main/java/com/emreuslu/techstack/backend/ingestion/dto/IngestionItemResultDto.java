package com.emreuslu.techstack.backend.ingestion.dto;

public record IngestionItemResultDto(
        boolean inserted,
        boolean softwareRelevant,
        int extractedSkillsCount,
        boolean companyReusedAfterDuplicate
) {

    public static IngestionItemResultDto skipped(boolean softwareRelevant) {
        return new IngestionItemResultDto(false, softwareRelevant, 0, false);
    }

    public static IngestionItemResultDto inserted(int extractedSkillsCount, boolean companyReusedAfterDuplicate) {
        return new IngestionItemResultDto(true, true, extractedSkillsCount, companyReusedAfterDuplicate);
    }
}

