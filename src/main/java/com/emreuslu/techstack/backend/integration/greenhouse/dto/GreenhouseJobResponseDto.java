package com.emreuslu.techstack.backend.integration.greenhouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GreenhouseJobResponseDto(
        Long id,
        String title,
        LocationDto location,
        @JsonProperty("absolute_url") String absoluteUrl,
        @JsonProperty("first_published") String firstPublished,
        @JsonProperty("updated_at") String updatedAt,
        @JsonProperty("content") String content,
        @JsonProperty("company_name") String companyName,
        List<DepartmentDto> departments,
        List<OfficeDto> offices,
        List<MetadataDto> metadata
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LocationDto(
            String name
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DepartmentDto(
            String name
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OfficeDto(
            String name
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetadataDto(
            String name,
            JsonNode value
    ) {
    }
}

