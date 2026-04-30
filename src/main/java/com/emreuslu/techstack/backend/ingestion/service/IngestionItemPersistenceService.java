package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.company.entity.Company;
import com.emreuslu.techstack.backend.company.service.CompanyService;
import com.emreuslu.techstack.backend.company.service.ExternalCompanyResolutionResult;
import com.emreuslu.techstack.backend.extraction.dto.ExtractedSkillDto;
import com.emreuslu.techstack.backend.extraction.service.SkillExtractionService;
import com.emreuslu.techstack.backend.ingestion.dto.IngestionItemResultDto;
import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import com.emreuslu.techstack.backend.jobskill.service.JobSkillService;
import com.emreuslu.techstack.backend.skill.entity.Skill;
import com.emreuslu.techstack.backend.skill.service.SkillService;
import com.emreuslu.techstack.backend.skill.service.SkillAliasService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IngestionItemPersistenceService {

    private final CompanyService companyService;
    private final JobRepository jobRepository;
    private final SkillExtractionService skillExtractionService;
    private final SkillService skillService;
    private final SkillAliasService skillAliasService;
    private final JobSkillService jobSkillService;
    private final RawJobPayloadService rawJobPayloadService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IngestionItemResultDto persistOne(NormalizedJobDto dto) {
        validateRequiredFields(dto);

        String externalId = cleanRequired(dto.externalJobId(), "externalJobId");
        String source = cleanRequired(dto.source(), "source");

        // Save raw payload for audit/debug if available
        if (dto.rawJobJson() != null) {
            try {
                rawJobPayloadService.saveRawPayload(source, externalId, null, dto.rawJobJson());
            } catch (Exception e) {
                // Log but don't fail - raw payload is for debugging, not critical
            }
        }

        if (!dto.isSoftwareRelevant()) {
            return IngestionItemResultDto.skipped(false);
        }

        if (jobRepository.findByExternalIdAndSource(externalId, source).isPresent()) {
            return IngestionItemResultDto.skipped(true);
        }

        ExternalCompanyResolutionResult companyResolution = companyService.getOrCreateExternalCompany(
                source,
                dto.externalCompanyId() != null ? dto.externalCompanyId() : dto.companyName(),
                dto.companyName()
        );
        Company company = companyResolution.company();

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
            return IngestionItemResultDto.inserted(0, companyResolution.reusedAfterDuplicate());
        }

        List<Skill> skills = extractedSkillDtos.stream()
                .map(ExtractedSkillDto::name)
                .map(this::resolveSkillAlias)
                .map(skillService::findOrCreateByName)
                .toList();

        jobSkillService.linkSkillsToJob(savedJob, skills);
        return IngestionItemResultDto.inserted(skills.size(), companyResolution.reusedAfterDuplicate());
    }

    private String resolveSkillAlias(String skillName) {
        // If skill has an alias, resolve to canonical name
        var canonicalSkill = skillAliasService.resolveAlias(skillName);
        return canonicalSkill.map(Skill::getName).orElse(skillName);
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
}

