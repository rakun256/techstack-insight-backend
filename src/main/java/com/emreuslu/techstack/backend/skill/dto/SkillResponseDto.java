package com.emreuslu.techstack.backend.skill.dto;

import java.time.Instant;

public record SkillResponseDto(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}

