package com.emreuslu.techstack.backend.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateJobRequestDto(
        @NotBlank String externalId,
        @NotBlank String source,
        @NotBlank String title,
        @NotBlank String location,
        @NotBlank String description,
        @NotBlank String applyUrl,
        @NotNull LocalDate postedAt,
        @NotNull UUID companyId
) {
}

