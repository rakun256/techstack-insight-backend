package com.emreuslu.techstack.backend.integration.lever.mapper;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.ingestion.dto.RoleClassificationResultDto;
import com.emreuslu.techstack.backend.ingestion.service.SoftwareRoleClassificationService;
import com.emreuslu.techstack.backend.ingestion.service.TextNormalizationService;
import com.emreuslu.techstack.backend.integration.lever.dto.LeverJobResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeverJobMapper {

    private static final String SOURCE = "LEVER";

    private final TextNormalizationService textNormalizationService;
    private final SoftwareRoleClassificationService softwareRoleClassificationService;

    public List<NormalizedJobDto> toNormalizedJobs(Collection<LeverJobResponseDto> jobs, String companyToken) {
        if (jobs == null || jobs.isEmpty()) {
            return List.of();
        }

        return jobs.stream()
                .map(job -> toNormalizedJob(job, companyToken))
                .toList();
    }

    public NormalizedJobDto toNormalizedJob(LeverJobResponseDto job, String companyToken) {
        String rawTitle = cleanOptional(job.text());
        String companyName = cleanOptional(companyToken);
        String location = job.categories() != null ? cleanOptional(job.categories().location()) : null;
        String department = job.categories() != null ? cleanOptional(job.categories().department()) : null;
        String team = job.categories() != null ? cleanOptional(job.categories().team()) : null;

        String descriptionPlain = textNormalizationService.mergeSections(textNormalizationService.nonNullSections(
                cleanOptional(job.descriptionPlain()),
                cleanOptional(job.descriptionBodyPlain()),
                cleanOptional(job.openingPlain()),
                cleanOptional(job.description()),
                joinListContents(job.lists())
        ));

        String analysisText = textNormalizationService.mergeSections(textNormalizationService.nonNullSections(
                rawTitle,
                descriptionPlain,
                department,
                team,
                location
        ));

        RoleClassificationResultDto classification = softwareRoleClassificationService.classify(
                rawTitle,
                department,
                team,
                analysisText,
                textNormalizationService
        );

        LocalDate postedAt = parsePostedAt(job.createdAt(), job.updatedAt());
        if (postedAt == null) {
            postedAt = LocalDate.now();
        }

        // Capture raw job JSON for audit trail
        String rawJobJson = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            rawJobJson = mapper.writeValueAsString(job);
        } catch (JsonProcessingException e) {
            log.debug("Could not serialize Lever job to JSON for audit: {}", e.getMessage());
        }

        return new NormalizedJobDto(
                SOURCE,
                cleanOptional(job.id()),
                null,
                companyName,
                rawTitle,
                classification.normalizedTitle(),
                classification.roleFamily(),
                classification.roleSubfamily(),
                classification.softwareRelevant(),
                classification.relevanceScore(),
                classification.relevanceReason(),
                location,
                location,
                null,
                isRemote(location),
                isHybrid(location),
                descriptionPlain,
                analysisText,
                resolveApplyUrl(job),
                postedAt,
                department,
                team,
                SOURCE + ":" + companyToken + ":" + cleanOptional(job.id()),
                SOURCE + ":" + cleanOptional(job.id()),
                null,
                rawJobJson
        );
    }

    private String joinListContents(List<LeverJobResponseDto.ListSectionDto> sections) {
        if (sections == null || sections.isEmpty()) {
            return null;
        }

        List<String> values = new ArrayList<>();
        for (LeverJobResponseDto.ListSectionDto section : sections) {
            if (section == null) {
                continue;
            }

            values.add(cleanOptional(section.text()));
            values.add(normalizeListContent(section.content()));
        }

        return textNormalizationService.mergeSections(values);
    }

    private String normalizeListContent(JsonNode content) {
        if (content == null || content.isNull() || content.isMissingNode()) {
            return null;
        }

        if (content.isTextual() || content.isNumber() || content.isBoolean()) {
            return cleanOptional(content.asText());
        }

        if (content.isArray()) {
            List<String> values = new ArrayList<>();
            for (JsonNode item : content) {
                values.add(normalizeListContent(item));
            }
            return textNormalizationService.mergeSections(values);
        }

        if (content.isObject()) {
            List<String> values = new ArrayList<>();
            Iterator<String> fields = content.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                values.add(cleanOptional(field));
                values.add(normalizeListContent(content.get(field)));
            }
            return textNormalizationService.mergeSections(values);
        }

        return cleanOptional(content.toString());
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
        return textNormalizationService.clean(value);
    }

    private boolean isRemote(String locationRaw) {
        String lower = locationRaw == null ? null : locationRaw.toLowerCase();
        return lower != null && lower.contains("remote");
    }

    private boolean isHybrid(String locationRaw) {
        String lower = locationRaw == null ? null : locationRaw.toLowerCase();
        return lower != null && lower.contains("hybrid");
    }
}

