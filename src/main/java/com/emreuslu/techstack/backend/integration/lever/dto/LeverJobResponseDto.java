package com.emreuslu.techstack.backend.integration.lever.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeverJobResponseDto(
        String id,
        String text,
        String description,
        @JsonProperty("descriptionPlain") String descriptionPlain,
        @JsonProperty("descriptionBodyPlain") String descriptionBodyPlain,
        @JsonProperty("openingPlain") String openingPlain,
        @JsonProperty("hostedUrl") String hostedUrl,
        @JsonProperty("applyUrl") String applyUrl,
        Long createdAt,
        Long updatedAt,
        CategoriesDto categories,
        List<ListSectionDto> lists
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CategoriesDto(
            String location,
            String department,
            String team,
            String commitment
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListSectionDto(
            String text,
            JsonNode content
    ) {
    }
}

