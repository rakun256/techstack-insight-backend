package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.integration.greenhouse.client.GreenhouseClient;
import com.emreuslu.techstack.backend.integration.greenhouse.mapper.GreenhouseJobMapper;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobIngestionFacade {

    private final IngestionService ingestionService;
    private final GreenhouseClient greenhouseClient;
    private final GreenhouseJobMapper greenhouseJobMapper;

    public void ingestNormalizedJobs(Collection<NormalizedJobDto> jobs) {
        ingestionService.ingestAll(jobs);
    }

    public void ingestFromGreenhouse(String boardToken) {
        String normalizedBoardToken = Objects.requireNonNull(boardToken, "boardToken must not be null").trim();
        if (normalizedBoardToken.isEmpty()) {
            throw new IllegalArgumentException("boardToken must not be blank");
        }

        List<NormalizedJobDto> normalizedJobs = greenhouseJobMapper.toNormalizedJobs(
                greenhouseClient.fetchJobs(normalizedBoardToken),
                normalizedBoardToken
        );

        ingestNormalizedJobs(normalizedJobs);
    }

    public void ingestFromLever() {
        // TODO: Fetch and normalize Lever records in integration.lever, then delegate here.
        ingestNormalizedJobs(List.of());
    }
}

