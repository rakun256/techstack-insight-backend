package com.emreuslu.techstack.backend.integration.lever.client;

import com.emreuslu.techstack.backend.integration.lever.dto.LeverJobResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class LeverClient {

    private final RestClient restClient;
    private final String postingsPath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LeverClient(
            @Value("${integration.lever.base-url:https://api.lever.co}") String baseUrl,
            @Value("${integration.lever.postings-path:/v0/postings/{companyToken}}") String postingsPath
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.postingsPath = postingsPath;
    }

    public List<LeverJobResponseDto> fetchJobs(String companyToken) {
        String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(postingsPath)
                        .queryParam("mode", "json")
                        .build(companyToken))
                .retrieve()
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            return List.of();
        }

        LeverJobResponseDto[] response;
        try {
            response = objectMapper.readValue(responseBody, LeverJobResponseDto[].class);
        } catch (JsonProcessingException exception) {
            log.error(
                    "lever_response_parse_failed token={} path={} cause={}",
                    companyToken,
                    postingsPath,
                    exception.getOriginalMessage()
            );
            throw new IllegalStateException("Failed to parse Lever jobs response for token: " + companyToken, exception);
        }

        if (response == null || response.length == 0) {
            return List.of();
        }

        return Arrays.asList(response);
    }
}

