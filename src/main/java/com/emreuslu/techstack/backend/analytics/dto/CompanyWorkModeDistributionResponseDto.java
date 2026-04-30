package com.emreuslu.techstack.backend.analytics.dto;

import java.util.UUID;

public record CompanyWorkModeDistributionResponseDto(
        UUID companyId,
        String companyName,
        String workMode,
        Long jobCount
) {
}

