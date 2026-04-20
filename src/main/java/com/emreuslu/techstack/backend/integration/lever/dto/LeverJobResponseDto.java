package com.emreuslu.techstack.backend.integration.lever.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeverJobResponseDto(
        String id,
        String text,
        String description,
        @JsonProperty("descriptionPlain") String descriptionPlain,
        @JsonProperty("hostedUrl") String hostedUrl,
        @JsonProperty("applyUrl") String applyUrl,
        Long createdAt,
        Long updatedAt,
        CategoriesDto categories
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CategoriesDto(
            String location,
            String team,
            String commitment
    ) {
    }
}

