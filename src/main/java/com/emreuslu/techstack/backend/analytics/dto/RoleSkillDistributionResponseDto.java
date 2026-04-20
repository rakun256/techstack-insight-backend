package com.emreuslu.techstack.backend.analytics.dto;

public record RoleSkillDistributionResponseDto(
        String jobTitle,
        String skillName,
        Long jobCount
) {
}

