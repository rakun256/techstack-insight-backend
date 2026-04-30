package com.emreuslu.techstack.backend.analytics.dto;

import java.util.UUID;

public record CompanyRoleDistributionResponseDto(
        UUID companyId,
        String companyName,
        String roleFamily,
        Long jobCount
) {
}

