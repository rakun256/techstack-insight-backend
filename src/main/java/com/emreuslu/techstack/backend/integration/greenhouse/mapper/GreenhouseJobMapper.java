package com.emreuslu.techstack.backend.integration.greenhouse.mapper;

import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.ingestion.dto.RoleClassificationResultDto;
import com.emreuslu.techstack.backend.ingestion.service.SoftwareRoleClassificationService;
import com.emreuslu.techstack.backend.ingestion.service.TextNormalizationService;
import com.emreuslu.techstack.backend.integration.greenhouse.dto.GreenhouseJobResponseDto;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GreenhouseJobMapper {

    private static final String SOURCE = "GREENHOUSE";

    private final TextNormalizationService textNormalizationService;
    private final SoftwareRoleClassificationService softwareRoleClassificationService;

    public List<NormalizedJobDto> toNormalizedJobs(Collection<GreenhouseJobResponseDto> jobs, String boardToken) {
        if (jobs == null || jobs.isEmpty()) {
            return List.of();
        }

        return jobs.stream()
                .map(job -> toNormalizedJob(job, boardToken))
                .toList();
    }

    public NormalizedJobDto toNormalizedJob(GreenhouseJobResponseDto job, String boardToken) {
        String rawTitle = textNormalizationService.clean(job.title());
        String companyName = resolveCompanyName(job, boardToken);
        String locationRaw = job.location() != null ? textNormalizationService.clean(job.location().name()) : null;
        String departmentRaw = joinDepartmentNames(job.departments());
        String officeRaw = joinOfficeNames(job.offices());
        String metadataRaw = joinMetadataValues(job.metadata());

        String descriptionPlain = textNormalizationService.toPlainText(job.content());
        String analysisText = textNormalizationService.mergeSections(textNormalizationService.nonNullSections(
                rawTitle,
                descriptionPlain,
                departmentRaw,
                officeRaw,
                metadataRaw,
                locationRaw
        ));

        RoleClassificationResultDto classification = softwareRoleClassificationService.classify(
                rawTitle,
                departmentRaw,
                null,
                analysisText,
                textNormalizationService
        );

        LocalDate postedAt = parsePostedAt(job.firstPublished());
        if (postedAt == null) {
            postedAt = parsePostedAt(job.updatedAt());
        }
        if (postedAt == null) {
            postedAt = LocalDate.now();
        }

        return new NormalizedJobDto(
                SOURCE,
                job.id() != null ? String.valueOf(job.id()) : null,
                null,
                companyName,
                rawTitle,
                classification.normalizedTitle(),
                classification.roleFamily(),
                classification.roleSubfamily(),
                classification.softwareRelevant(),
                classification.relevanceScore(),
                classification.relevanceReason(),
                locationRaw,
                locationRaw,
                null,
                isRemote(locationRaw),
                isHybrid(locationRaw),
                descriptionPlain,
                analysisText,
                cleanOptional(job.absoluteUrl()),
                postedAt,
                departmentRaw,
                null,
                SOURCE + ":" + boardToken + ":" + job.id(),
                SOURCE + ":" + job.id(),
                metadataRaw
        );
    }

    private String resolveCompanyName(GreenhouseJobResponseDto job, String boardToken) {
        String companyName = cleanOptional(job.companyName());
        return companyName != null ? companyName : cleanOptional(boardToken);
    }

    private String joinDepartmentNames(List<GreenhouseJobResponseDto.DepartmentDto> departments) {
        if (departments == null || departments.isEmpty()) {
            return null;
        }

        List<String> sections = new ArrayList<>();
        for (GreenhouseJobResponseDto.DepartmentDto department : departments) {
            if (department != null && department.name() != null) {
                sections.add(department.name());
            }
        }
        return textNormalizationService.mergeSections(sections);
    }

    private String joinOfficeNames(List<GreenhouseJobResponseDto.OfficeDto> offices) {
        if (offices == null || offices.isEmpty()) {
            return null;
        }

        List<String> sections = new ArrayList<>();
        for (GreenhouseJobResponseDto.OfficeDto office : offices) {
            if (office != null && office.name() != null) {
                sections.add(office.name());
            }
        }
        return textNormalizationService.mergeSections(sections);
    }

    private String joinMetadataValues(List<GreenhouseJobResponseDto.MetadataDto> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        List<String> sections = new ArrayList<>();
        for (GreenhouseJobResponseDto.MetadataDto entry : metadata) {
            if (entry == null) {
                continue;
            }
            sections.add(textNormalizationService.clean(entry.name()));
            sections.add(textNormalizationService.clean(entry.value()));
        }
        return textNormalizationService.mergeSections(sections);
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

