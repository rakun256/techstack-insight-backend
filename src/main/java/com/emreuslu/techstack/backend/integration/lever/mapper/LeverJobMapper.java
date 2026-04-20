package com.emreuslu.techstack.backend.integration.lever.mapper;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.integration.lever.dto.LeverJobResponseDto;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LeverJobMapper {

    private static final String SOURCE = "LEVER";

    public List<NormalizedJobDto> toNormalizedJobs(Collection<LeverJobResponseDto> jobs, String companyToken) {
        if (jobs == null || jobs.isEmpty()) {
            return List.of();
        }

        return jobs.stream()
                .map(job -> toNormalizedJob(job, companyToken))
                .toList();
    }

    public NormalizedJobDto toNormalizedJob(LeverJobResponseDto job, String companyToken) {
        String companyName = cleanOptional(companyToken);
        String location = job.categories() != null ? cleanOptional(job.categories().location()) : null;

        return new NormalizedJobDto(
                cleanOptional(job.id()),
                SOURCE,
                null,
                companyName,
                cleanOptional(job.text()),
                location,
                resolveDescription(job),
                resolveApplyUrl(job),
                parsePostedAt(job.createdAt(), job.updatedAt()),
                null
        );
    }

    private String resolveDescription(LeverJobResponseDto job) {
        String plain = cleanOptional(job.descriptionPlain());
        if (plain != null) {
            return plain;
        }
        return cleanOptional(job.description());
    }

    private String resolveApplyUrl(LeverJobResponseDto job) {
        String applyUrl = cleanOptional(job.applyUrl());
        if (applyUrl != null) {
            return applyUrl;
        }
        return cleanOptional(job.hostedUrl());
    }

    private LocalDate parsePostedAt(Long createdAt, Long updatedAt) {
        Long epoch = createdAt != null ? createdAt : updatedAt;
        if (epoch == null) {
            return null;
        }

        // TODO: Confirm timestamp unit for the selected Lever account and simplify this conversion.
        long epochMillis = epoch > 9_999_999_999L ? epoch : epoch * 1000;
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
    }

    private String cleanOptional(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim().replaceAll("\\s+", " ");
        return cleaned.isEmpty() ? null : cleaned;
    }
}

