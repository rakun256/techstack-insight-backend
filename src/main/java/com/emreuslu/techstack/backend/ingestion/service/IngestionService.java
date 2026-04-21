package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.company.entity.Company;
import com.emreuslu.techstack.backend.company.repository.CompanyRepository;
import com.emreuslu.techstack.backend.ingestion.dto.IngestionRunStatsDto;
import com.emreuslu.techstack.backend.extraction.dto.ExtractedSkillDto;
import com.emreuslu.techstack.backend.extraction.service.SkillExtractionService;
import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import com.emreuslu.techstack.backend.jobskill.service.JobSkillService;
import com.emreuslu.techstack.backend.skill.entity.Skill;
import com.emreuslu.techstack.backend.skill.service.SkillService;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final SkillExtractionService skillExtractionService;
    private final SkillService skillService;
    private final JobSkillService jobSkillService;

    @Transactional
    public IngestionRunStatsDto ingestAll(Collection<NormalizedJobDto> jobs, String source, String token) {
        if (jobs == null || jobs.isEmpty()) {
            return IngestionRunStatsDto.empty(source, token);
        }

        int inserted = 0;
        int skipped = 0;
        int softwareRelevant = 0;
        int extractedSkills = 0;
        int failures = 0;

        for (NormalizedJobDto job : jobs) {
            if (job == null) {
                continue;
            }

            try {
                IngestionOutcome outcome = ingestOne(job);
                if (outcome.inserted()) {
                    inserted++;
                    extractedSkills += outcome.extractedSkillsCount();
                } else {
                    skipped++;
                }
                if (outcome.softwareRelevant()) {
                    softwareRelevant++;
                }
            } catch (Exception exception) {
                failures++;
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
                failures
        );
    }

    @Transactional
    public IngestionRunStatsDto ingestAll(Collection<NormalizedJobDto> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return IngestionRunStatsDto.empty("UNKNOWN", "UNKNOWN");
        }

        String source = jobs.iterator().next() != null ? jobs.iterator().next().source() : "UNKNOWN";
        return ingestAll(jobs, source, "UNKNOWN");
    }

    @Transactional
    public IngestionOutcome ingestOne(NormalizedJobDto dto) {
        validateRequiredFields(dto);

        String externalId = cleanRequired(dto.externalJobId(), "externalJobId");
        String source = cleanRequired(dto.source(), "source");

        if (!dto.isSoftwareRelevant()) {
            return IngestionOutcome.skipped(false);
        }

        if (jobRepository.findByExternalIdAndSource(externalId, source).isPresent()) {
            return IngestionOutcome.skipped(true);
        }

        Company company = resolveCompany(dto, source);

        Job job = Job.builder()
                .externalId(externalId)
                .source(source)
                .title(cleanRequired(dto.normalizedTitle() != null ? dto.normalizedTitle() : dto.rawTitle(), "title"))
                .normalizedTitle(cleanOptional(dto.normalizedTitle()))
                .softwareRelevant(dto.isSoftwareRelevant())
                .roleFamily(cleanOptional(dto.roleFamily()))
                .roleSubfamily(cleanOptional(dto.roleSubfamily()))
                .location(cleanRequired(dto.locationNormalized() != null ? dto.locationNormalized() : dto.locationRaw(), "location"))
                .locationNormalized(cleanOptional(dto.locationNormalized()))
                .country(cleanOptional(dto.country()))
                .remote(dto.isRemote())
                .hybrid(dto.isHybrid())
                .description(cleanOptional(dto.descriptionPlainNormalized()) != null ? cleanOptional(dto.descriptionPlainNormalized()) : "")
                .applyUrl(cleanRequired(dto.applyUrl(), "applyUrl"))
                .postedAt(Objects.requireNonNull(dto.postedAt(), "postedAt must not be null"))
                .company(company)
                .build();

        Job savedJob = jobRepository.save(job);

        List<ExtractedSkillDto> extractedSkillDtos = skillExtractionService.extractSkills(
                dto.analysisText() != null ? dto.analysisText() : dto.descriptionPlainNormalized()
        );
        if (extractedSkillDtos.isEmpty()) {
            return IngestionOutcome.inserted(0, true);
        }

        List<Skill> skills = extractedSkillDtos.stream()
                .map(ExtractedSkillDto::name)
                .map(skillService::findOrCreateByName)
                .toList();

        jobSkillService.linkSkillsToJob(savedJob, skills);
        return IngestionOutcome.inserted(skills.size(), true);
    }

    private Company resolveCompany(NormalizedJobDto dto, String source) {
        String companyName = cleanRequired(dto.companyName(), "companyName");
        String companyExternalId = cleanOptional(dto.externalCompanyId());

        if (companyExternalId != null) {
            return companyRepository.findByExternalSourceAndExternalCompanyId(source, companyExternalId)
                    .or(() -> companyRepository.findByNameIgnoreCase(companyName))
                    .orElseGet(() -> createCompany(companyName, source, companyExternalId));
        }

        return companyRepository.findByNameIgnoreCase(companyName)
                .orElseGet(() -> {
                    // TODO: Replace fallback with source-aware external company mapping when integration clients are added.
                    String fallbackExternalId = companyName;
                    return createCompany(companyName, source, fallbackExternalId);
                });
    }

    private Company createCompany(String name, String source, String externalCompanyId) {
        Company company = Company.builder()
                .name(name)
                .externalSource(source)
                .externalCompanyId(externalCompanyId)
                .build();

        return companyRepository.save(company);
    }

    private void validateRequiredFields(NormalizedJobDto dto) {
        Objects.requireNonNull(dto, "normalized job must not be null");
        cleanRequired(dto.externalJobId(), "externalJobId");
        cleanRequired(dto.source(), "source");
        cleanRequired(dto.companyName(), "companyName");
        cleanRequired(dto.rawTitle(), "rawTitle");
        cleanRequired(dto.locationRaw(), "locationRaw");
        cleanRequired(dto.applyUrl(), "applyUrl");
        Objects.requireNonNull(dto.postedAt(), "postedAt must not be null");
    }

    private String cleanRequired(String value, String fieldName) {
        String cleaned = cleanOptional(value);
        if (cleaned == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return cleaned;
    }

    private String cleanOptional(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim().replaceAll("\\s+", " ");
        return cleaned.isEmpty() ? null : cleaned;
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

