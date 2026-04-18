package com.emreuslu.techstack.backend.company.dto;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponseDto(
        UUID id,
        String name,
        String externalSource,
        String externalCompanyId,
        Instant createdAt,
        Instant updatedAt
) {
}

