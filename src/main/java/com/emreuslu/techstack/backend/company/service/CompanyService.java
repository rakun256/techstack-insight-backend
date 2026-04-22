package com.emreuslu.techstack.backend.company.service;

import com.emreuslu.techstack.backend.common.exception.ResourceNotFoundException;
import com.emreuslu.techstack.backend.company.dto.CompanyResponseDto;
import com.emreuslu.techstack.backend.company.entity.Company;
import com.emreuslu.techstack.backend.company.repository.CompanyRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final PlatformTransactionManager transactionManager;

    @Transactional(readOnly = true)
    public CompanyResponseDto getById(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        // TODO: Extend mapping when company enrichment fields are introduced.
        return new CompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getExternalSource(),
                company.getExternalCompanyId(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }

    public ExternalCompanyResolutionResult getOrCreateExternalCompany(
            String externalSource,
            String externalCompanyId,
            String companyName
    ) {
        String normalizedSource = normalizeRequired(externalSource, "externalSource");
        String normalizedCompanyId = normalizeRequired(externalCompanyId, "externalCompanyId");
        String normalizedCompanyName = normalizeRequired(companyName, "companyName");

        Company existing = companyRepository
                .findByExternalSourceAndExternalCompanyId(normalizedSource, normalizedCompanyId)
                .orElse(null);
        if (existing != null) {
            return new ExternalCompanyResolutionResult(existing, false);
        }

        Company byNameFallback = companyRepository.findByNameIgnoreCase(normalizedCompanyName).orElse(null);
        if (byNameFallback != null) {
            return new ExternalCompanyResolutionResult(byNameFallback, false);
        }

        try {
            Company created = createCompanyInNewTransaction(normalizedSource, normalizedCompanyId, normalizedCompanyName);
            return new ExternalCompanyResolutionResult(created, false);
        } catch (DataIntegrityViolationException exception) {
            Company concurrentWinner = companyRepository
                    .findByExternalSourceAndExternalCompanyId(normalizedSource, normalizedCompanyId)
                    .orElseThrow(() -> exception);
            return new ExternalCompanyResolutionResult(concurrentWinner, true);
        }
    }

    private Company createCompanyInNewTransaction(
            String externalSource,
            String externalCompanyId,
            String companyName
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        return transactionTemplate.execute(status -> companyRepository.saveAndFlush(
                Company.builder()
                        .name(companyName)
                        .externalSource(externalSource)
                        .externalCompanyId(externalCompanyId)
                        .build()
        ));
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String cleaned = value.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return cleaned;
    }
}

