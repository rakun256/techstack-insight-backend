package com.emreuslu.techstack.backend.integration.greenhouse.client;

import com.emreuslu.techstack.backend.integration.greenhouse.dto.GreenhouseJobResponseDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class GreenhouseClient {

    private final RestClient restClient;
    private final String jobsPath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GreenhouseClient(
            @Value("${integration.greenhouse.base-url:https://boards-api.greenhouse.io}") String baseUrl,
            @Value("${integration.greenhouse.jobs-path:/v1/boards/{boardToken}/jobs}") String jobsPath
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.jobsPath = jobsPath;
    }

    public List<GreenhouseJobResponseDto> fetchJobs(String boardToken) {
        String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(jobsPath)
                        .queryParam("content", true)
                        .build(boardToken))
                .retrieve()
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            return List.of();
        }

        GreenhouseJobsApiResponse response;
        try {
            response = objectMapper.readValue(responseBody, GreenhouseJobsApiResponse.class);
        } catch (JsonProcessingException exception) {
            log.error(
                    "greenhouse_response_parse_failed token={} path={} cause={}",
                    boardToken,
                    jobsPath,
                    exception.getOriginalMessage()
            );
            throw new IllegalStateException("Failed to parse Greenhouse jobs response for token: " + boardToken, exception);
        }

        if (response == null || response.jobs() == null) {
            return List.of();
        }

        return response.jobs();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GreenhouseJobsApiResponse(
            List<GreenhouseJobResponseDto> jobs
    ) {
    }
}
