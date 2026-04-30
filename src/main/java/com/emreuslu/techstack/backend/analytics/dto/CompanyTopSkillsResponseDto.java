package com.emreuslu.techstack.backend.analytics.dto;

import java.util.UUID;

public record CompanyTopSkillsResponseDto(
        UUID companyId,
        String companyName,
        String skillName,
        Long jobCount
) {
}

