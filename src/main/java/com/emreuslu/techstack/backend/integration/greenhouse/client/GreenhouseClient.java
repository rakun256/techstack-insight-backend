package com.emreuslu.techstack.backend.integration.greenhouse.client;

import com.emreuslu.techstack.backend.integration.greenhouse.dto.GreenhouseJobResponseDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GreenhouseClient {

    private final RestClient restClient;
    private final String jobsPath;

    public GreenhouseClient(
            @Value("${integration.greenhouse.base-url:https://boards-api.greenhouse.io}") String baseUrl,
            @Value("${integration.greenhouse.jobs-path:/v1/boards/{boardToken}/jobs}") String jobsPath
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.jobsPath = jobsPath;
    }

    public List<GreenhouseJobResponseDto> fetchJobs(String boardToken) {
        GreenhouseJobsApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(jobsPath)
                        .queryParam("content", true)
                        .build(boardToken))
                .retrieve()
                .body(GreenhouseJobsApiResponse.class);

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
