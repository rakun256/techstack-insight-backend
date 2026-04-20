package com.emreuslu.techstack.backend.integration.lever.client;

import com.emreuslu.techstack.backend.integration.lever.dto.LeverJobResponseDto;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LeverClient {

    private final RestClient restClient;
    private final String postingsPath;

    public LeverClient(
            @Value("${integration.lever.base-url:https://api.lever.co}") String baseUrl,
            @Value("${integration.lever.postings-path:/v0/postings/{companyToken}}") String postingsPath
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.postingsPath = postingsPath;
    }

    public List<LeverJobResponseDto> fetchJobs(String companyToken) {
        LeverJobResponseDto[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(postingsPath)
                        .queryParam("mode", "json")
                        .build(companyToken))
                .retrieve()
                .body(LeverJobResponseDto[].class);

        if (response == null || response.length == 0) {
            return List.of();
        }

        return Arrays.asList(response);
    }
}

