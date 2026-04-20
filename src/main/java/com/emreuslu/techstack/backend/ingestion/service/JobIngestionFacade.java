package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobIngestionFacade {

    private final IngestionService ingestionService;

    public void ingestNormalizedJobs(Collection<NormalizedJobDto> jobs) {
        ingestionService.ingestAll(jobs);
    }

    public void ingestFromGreenhouse() {
        // TODO: Fetch and normalize Greenhouse records in integration.greenhouse, then delegate here.
        ingestNormalizedJobs(List.of());
    }

    public void ingestFromLever() {
        // TODO: Fetch and normalize Lever records in integration.lever, then delegate here.
        ingestNormalizedJobs(List.of());
    }
}

