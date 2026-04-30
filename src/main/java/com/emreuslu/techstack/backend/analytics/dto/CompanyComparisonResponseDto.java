package com.emreuslu.techstack.backend.analytics.dto;

import java.util.List;
import java.util.UUID;

public record CompanyComparisonResponseDto(
        CompanySnapshot companyA,
        CompanySnapshot companyB
) {
    public record CompanySnapshot(
            UUID companyId,
            String companyName,
            List<SkillEntry> topSkills,
            List<RoleEntry> roleDistribution,
            List<WorkModeEntry> workModeDistribution
    ) {
        public record SkillEntry(String skillName, Long jobCount) {}
        public record RoleEntry(String roleFamily, Long jobCount) {}
        public record WorkModeEntry(String workMode, Long jobCount) {}
    }
}

