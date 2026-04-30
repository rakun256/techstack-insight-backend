package com.emreuslu.techstack.backend.job.dto;

import java.time.LocalDate;
import java.util.UUID;

public record JobFilterCriteria(
        String roleFamily,
        String country,
        Boolean remote,
        Boolean hybrid,
        UUID companyId,
        Long skillId,
        String skillName,
        String source,
        LocalDate postedAtFrom,
        LocalDate postedAtTo,
        String titleQuery
) {

    public boolean hasAnyFilter() {
        return roleFamily != null || country != null || remote != null || hybrid != null ||
               companyId != null || skillId != null || skillName != null || source != null ||
               postedAtFrom != null || postedAtTo != null || titleQuery != null;
    }
}

