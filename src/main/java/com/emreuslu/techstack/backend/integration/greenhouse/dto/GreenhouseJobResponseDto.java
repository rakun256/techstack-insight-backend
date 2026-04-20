package com.emreuslu.techstack.backend.integration.greenhouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GreenhouseJobResponseDto(
        Long id,
        String title,
        LocationDto location,
        @JsonProperty("absolute_url") String absoluteUrl,
        @JsonProperty("updated_at") String updatedAt,
        @JsonProperty("content") String content,
        @JsonProperty("company_name") String companyName
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LocationDto(
            String name
    ) {
    }
}

