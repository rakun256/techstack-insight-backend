package com.emreuslu.techstack.backend.company.service;

import com.emreuslu.techstack.backend.common.exception.ResourceNotFoundException;
import com.emreuslu.techstack.backend.company.dto.CompanyResponseDto;
import com.emreuslu.techstack.backend.company.entity.Company;
import com.emreuslu.techstack.backend.company.repository.CompanyRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

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
}

