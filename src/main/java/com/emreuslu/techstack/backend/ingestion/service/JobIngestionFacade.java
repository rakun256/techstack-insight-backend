package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.integration.greenhouse.client.GreenhouseClient;
import com.emreuslu.techstack.backend.integration.greenhouse.mapper.GreenhouseJobMapper;
import com.emreuslu.techstack.backend.integration.lever.client.LeverClient;
import com.emreuslu.techstack.backend.integration.lever.mapper.LeverJobMapper;
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
    private final LeverClient leverClient;
    private final LeverJobMapper leverJobMapper;

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

    public void ingestFromLever(String companyToken) {
        String normalizedCompanyToken = Objects.requireNonNull(companyToken, "companyToken must not be null").trim();
        if (normalizedCompanyToken.isEmpty()) {
            throw new IllegalArgumentException("companyToken must not be blank");
        }

        List<NormalizedJobDto> normalizedJobs = leverJobMapper.toNormalizedJobs(
                leverClient.fetchJobs(normalizedCompanyToken),
                normalizedCompanyToken
        );

        ingestNormalizedJobs(normalizedJobs);
    }
}

