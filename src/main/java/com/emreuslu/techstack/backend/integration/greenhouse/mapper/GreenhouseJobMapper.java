package com.emreuslu.techstack.backend.integration.greenhouse.mapper;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.integration.greenhouse.dto.GreenhouseJobResponseDto;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GreenhouseJobMapper {

    private static final String SOURCE = "GREENHOUSE";

    public List<NormalizedJobDto> toNormalizedJobs(Collection<GreenhouseJobResponseDto> jobs, String boardToken) {
        if (jobs == null || jobs.isEmpty()) {
            return List.of();
        }

        return jobs.stream()
                .map(job -> toNormalizedJob(job, boardToken))
                .toList();
    }

    public NormalizedJobDto toNormalizedJob(GreenhouseJobResponseDto job, String boardToken) {
        String companyName = cleanOptional(job.companyName());
        if (companyName == null) {
            companyName = cleanOptional(boardToken);
        }

        String locationName = job.location() != null ? cleanOptional(job.location().name()) : null;

        return new NormalizedJobDto(
                job.id() != null ? String.valueOf(job.id()) : null,
                SOURCE,
                null,
                companyName,
                cleanOptional(job.title()),
                locationName,
                cleanOptional(job.content()),
                cleanOptional(job.absoluteUrl()),
                parsePostedAt(job.updatedAt()),
                null
        );
    }

    private LocalDate parsePostedAt(String updatedAt) {
        String value = cleanOptional(updatedAt);
        if (value == null) {
            return null;
        }

        try {
            return OffsetDateTime.parse(value).toLocalDate();
        } catch (DateTimeParseException exception) {
            // TODO: Revisit parsing when Greenhouse payload contract is fixed for target boards.
            return null;
        }
    }

    private String cleanOptional(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim().replaceAll("\\s+", " ");
        return cleaned.isEmpty() ? null : cleaned;
    }
}

