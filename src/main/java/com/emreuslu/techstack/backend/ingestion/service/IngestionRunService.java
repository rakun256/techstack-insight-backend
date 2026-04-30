package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.dto.IngestionRunStatsDto;
import com.emreuslu.techstack.backend.ingestion.entity.IngestionRun;
import com.emreuslu.techstack.backend.ingestion.repository.IngestionRunRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unused")  // Future admin dashboard queries
public class IngestionRunService {

    private final IngestionRunRepository ingestionRunRepository;

    public IngestionRun saveIngestionRun(IngestionRunStatsDto stats, String triggerType) {
        return saveIngestionRun(stats, triggerType, null);
    }

    public IngestionRun saveIngestionRun(IngestionRunStatsDto stats, String triggerType, String failureReason) {
        IngestionRun run = IngestionRun.builder()
                .source(stats.source())
                .token(stats.token())
                .triggerType(triggerType)
                .fetchedCount(stats.fetchedCount())
                .insertedCount(stats.insertedCount())
                .skippedCount(stats.skippedCount())
                .softwareRelevantCount(stats.softwareRelevantCount())
                .extractedSkillsCount(stats.extractedSkillsCount())
                .failedCount(stats.failureCount())
                .companyReusedAfterDuplicateCount(stats.companyReusedAfterDuplicateCount())
                .status(stats.status())
                .failureReason(failureReason)
                .runDurationMs(stats.runDurationMs())
                .finishedAt(Instant.now())
                .build();

        return ingestionRunRepository.save(run);
    }

    public List<IngestionRun> getRecentRuns(String source, String token, int limitDays) {
        Instant since = Instant.now().minusSeconds(limitDays * 24L * 60 * 60);
        return ingestionRunRepository.findRecentRuns(source, token, since);
    }

    public List<IngestionRun> getRunsBySource(String source) {
        return ingestionRunRepository.findBySourceOrderByStartedAtDesc(source);
    }

    public List<IngestionRun> getRunsByStatus(String status) {
        return ingestionRunRepository.findByStatus(status);
    }
}

