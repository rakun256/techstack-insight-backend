package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.ingestion.dto.IngestionProperties;
import com.emreuslu.techstack.backend.ingestion.dto.IngestionRunStatsDto;
import com.emreuslu.techstack.backend.integration.greenhouse.client.GreenhouseClient;
import com.emreuslu.techstack.backend.integration.greenhouse.mapper.GreenhouseJobMapper;
import com.emreuslu.techstack.backend.integration.lever.client.LeverClient;
import com.emreuslu.techstack.backend.integration.lever.mapper.LeverJobMapper;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobIngestionFacade {

    private static final Set<String> RUN_GUARD = ConcurrentHashMap.newKeySet();

    private final IngestionService ingestionService;
    private final GreenhouseClient greenhouseClient;
    private final GreenhouseJobMapper greenhouseJobMapper;
    private final LeverClient leverClient;
    private final LeverJobMapper leverJobMapper;

    public IngestionRunStatsDto ingestNormalizedJobs(Collection<NormalizedJobDto> jobs, String source, String token) {
        long startedAt = System.currentTimeMillis();
        IngestionRunStatsDto stats = ingestionService.ingestAll(jobs, source, token);
        long duration = System.currentTimeMillis() - startedAt;
        String status = stats.failureCount() > 0 ? "PARTIAL_SUCCESS" : "SUCCESS";
        IngestionRunStatsDto finalized = stats.withDurationAndStatus(duration, status);
        log.info(
                "ingestion_summary source={} token={} fetched={} inserted={} skipped={} softwareRelevant={} extractedSkills={} failures={} companyReusedAfterDuplicate={} durationMs={} status={}",
                finalized.source(),
                finalized.token(),
                finalized.fetchedCount(),
                finalized.insertedCount(),
                finalized.skippedCount(),
                finalized.softwareRelevantCount(),
                finalized.extractedSkillsCount(),
                finalized.failureCount(),
                finalized.companyReusedAfterDuplicateCount(),
                finalized.runDurationMs(),
                finalized.status()
        );
        return finalized;
    }

    public IngestionRunStatsDto ingestFromGreenhouse(String boardToken) {
        String normalizedBoardToken = Objects.requireNonNull(boardToken, "boardToken must not be null").trim();
        if (normalizedBoardToken.isEmpty()) {
            throw new IllegalArgumentException("boardToken must not be blank");
        }

        List<NormalizedJobDto> normalizedJobs = greenhouseJobMapper.toNormalizedJobs(
                greenhouseClient.fetchJobs(normalizedBoardToken),
                normalizedBoardToken
        );

        return ingestNormalizedJobs(normalizedJobs, "GREENHOUSE", normalizedBoardToken);
    }

    public IngestionRunStatsDto ingestFromLever(String companyToken) {
        String normalizedCompanyToken = Objects.requireNonNull(companyToken, "companyToken must not be null").trim();
        if (normalizedCompanyToken.isEmpty()) {
            throw new IllegalArgumentException("companyToken must not be blank");
        }

        List<NormalizedJobDto> normalizedJobs = leverJobMapper.toNormalizedJobs(
                leverClient.fetchJobs(normalizedCompanyToken),
                normalizedCompanyToken
        );

        return ingestNormalizedJobs(normalizedJobs, "LEVER", normalizedCompanyToken);
    }

    public IngestionRunStatsDto ingestConfiguredSource(String type, String token) {
        String normalizedType = Objects.requireNonNull(type, "type must not be null").trim().toUpperCase();
        String normalizedToken = Objects.requireNonNull(token, "token must not be null").trim();
        if (normalizedToken.isEmpty()) {
            throw new IllegalArgumentException("token must not be blank");
        }

        String guardKey = normalizedType + ":" + normalizedToken;
        if (!RUN_GUARD.add(guardKey)) {
            log.warn("ingestion_overlap_skipped source={} token={}", normalizedType, normalizedToken);
            return new IngestionRunStatsDto(
                    normalizedType,
                    normalizedToken,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0L,
                    "SKIPPED_ALREADY_RUNNING"
            );
        }

        try {
            return switch (normalizedType) {
                case "GREENHOUSE" -> ingestFromGreenhouse(normalizedToken);
                case "LEVER" -> ingestFromLever(normalizedToken);
                default -> throw new IllegalArgumentException("Unsupported source type: " + type);
            };
        } finally {
            RUN_GUARD.remove(guardKey);
        }
    }

    public List<IngestionRunStatsDto> ingestAllConfiguredSources(List<IngestionProperties.Source> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }

        return sources.stream()
                .filter(IngestionProperties.Source::isEnabled)
                .map(source -> ingestConfiguredSource(source.getType(), source.getToken()))
                .toList();
    }
}

