package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.dto.IngestionItemResultDto;
import com.emreuslu.techstack.backend.ingestion.dto.IngestionRunStatsDto;
import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final IngestionItemPersistenceService ingestionItemPersistenceService;

    public IngestionRunStatsDto ingestAll(Collection<NormalizedJobDto> jobs, String source, String token) {
        if (jobs == null || jobs.isEmpty()) {
            return IngestionRunStatsDto.empty(source, token);
        }

        int inserted = 0;
        int skipped = 0;
        int softwareRelevant = 0;
        int extractedSkills = 0;
        int failures = 0;
        int companyReusedAfterDuplicate = 0;

        for (NormalizedJobDto job : jobs) {
            if (job == null) {
                continue;
            }

            try {
                IngestionItemResultDto outcome = ingestionItemPersistenceService.persistOne(job);
                if (outcome.inserted()) {
                    inserted++;
                    extractedSkills += outcome.extractedSkillsCount();
                    if (outcome.companyReusedAfterDuplicate()) {
                        companyReusedAfterDuplicate++;
                    }
                } else {
                    skipped++;
                }
                if (outcome.softwareRelevant()) {
                    softwareRelevant++;
                }
            } catch (Exception exception) {
                failures++;
                log.warn(
                        "ingestion_item_failed source={} token={} externalJobId={} externalCompanyId={} companyName={} reason={}",
                        source,
                        token,
                        safe(job.externalJobId()),
                        safe(job.externalCompanyId()),
                        safe(job.companyName()),
                        exception.getMessage()
                );
            }
        }

        return new IngestionRunStatsDto(
                source,
                token,
                jobs.size(),
                inserted,
                skipped,
                softwareRelevant,
                extractedSkills,
                failures,
                companyReusedAfterDuplicate,
                0L,
                failures > 0 ? "PARTIAL_SUCCESS" : "SUCCESS"
        );
    }

    public IngestionRunStatsDto ingestAll(Collection<NormalizedJobDto> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return IngestionRunStatsDto.empty("UNKNOWN", "UNKNOWN");
        }

        String source = jobs.iterator().next() != null ? jobs.iterator().next().source() : "UNKNOWN";
        return ingestAll(jobs, source, "UNKNOWN");
    }

    public IngestionOutcome ingestOne(NormalizedJobDto dto) {
        IngestionItemResultDto result = ingestionItemPersistenceService.persistOne(dto);
        return new IngestionOutcome(result.inserted(), result.extractedSkillsCount(), result.softwareRelevant());
    }

    private String safe(String value) {
        return value == null ? "UNKNOWN" : value;
    }

    public record IngestionOutcome(boolean inserted, int extractedSkillsCount, boolean softwareRelevant) {

        public static IngestionOutcome skipped(boolean softwareRelevant) {
            return new IngestionOutcome(false, 0, softwareRelevant);
        }

        public static IngestionOutcome inserted(int extractedSkillsCount, boolean softwareRelevant) {
            return new IngestionOutcome(true, extractedSkillsCount, softwareRelevant);
        }
    }
}

