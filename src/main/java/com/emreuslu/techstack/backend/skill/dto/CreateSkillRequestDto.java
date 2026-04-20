package com.emreuslu.techstack.backend.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSkillRequestDto(
        @NotBlank
        @Size(max = 255)
        String name
) {
}

