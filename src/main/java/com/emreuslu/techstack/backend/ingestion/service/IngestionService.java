package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.company.entity.Company;
import com.emreuslu.techstack.backend.company.repository.CompanyRepository;
import com.emreuslu.techstack.backend.ingestion.dto.NormalizedJobDto;
import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import java.util.Collection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    @Transactional
    public void ingestAll(Collection<NormalizedJobDto> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return;
        }

        for (NormalizedJobDto job : jobs) {
            if (job != null) {
                ingestOne(job);
            }
        }
    }

    @Transactional
    public void ingestOne(NormalizedJobDto dto) {
        validateRequiredFields(dto);

        String externalId = cleanRequired(dto.externalId(), "externalId");
        String source = cleanRequired(dto.source(), "source");

        if (jobRepository.findByExternalIdAndSource(externalId, source).isPresent()) {
            return;
        }

        Company company = resolveCompany(dto, source);

        Job job = Job.builder()
                .externalId(externalId)
                .source(source)
                .title(cleanRequired(dto.title(), "title"))
                .location(cleanRequired(dto.location(), "location"))
                .description(cleanRequired(dto.description(), "description"))
                .applyUrl(cleanRequired(dto.applyUrl(), "applyUrl"))
                .postedAt(Objects.requireNonNull(dto.postedAt(), "postedAt must not be null"))
                .company(company)
                .build();

        jobRepository.save(job);

        // TODO: Plug extraction pipeline here to detect skills and persist JobSkill links.
    }

    private Company resolveCompany(NormalizedJobDto dto, String source) {
        String companyName = cleanRequired(dto.companyName(), "companyName");
        String companyExternalId = cleanOptional(dto.companyExternalId());

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
        cleanRequired(dto.externalId(), "externalId");
        cleanRequired(dto.source(), "source");
        cleanRequired(dto.companyName(), "companyName");
        cleanRequired(dto.title(), "title");
        cleanRequired(dto.location(), "location");
        cleanRequired(dto.description(), "description");
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

