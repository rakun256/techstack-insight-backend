package com.emreuslu.techstack.backend.company.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.emreuslu.techstack.backend.company.entity.Company;
import com.emreuslu.techstack.backend.company.repository.CompanyRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

class CompanyServiceTest {

    private CompanyRepository companyRepository;
    private PlatformTransactionManager transactionManager;
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        companyRepository = Mockito.mock(CompanyRepository.class);
        transactionManager = Mockito.mock(PlatformTransactionManager.class);

        TransactionStatus transactionStatus = new SimpleTransactionStatus();
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(transactionStatus);

        companyService = new CompanyService(companyRepository, transactionManager);
    }

    @Test
    void reusesCompanyWhenConcurrentCreateHitsUniqueConstraint() {
        Company existing = Company.builder()
                .id(UUID.randomUUID())
                .name("Vercel")
                .externalSource("GREENHOUSE")
                .externalCompanyId("Vercel")
                .build();

        when(companyRepository.findByExternalSourceAndExternalCompanyId("GREENHOUSE", "Vercel"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(companyRepository.findByNameIgnoreCase("Vercel")).thenReturn(Optional.empty());
        when(companyRepository.saveAndFlush(any(Company.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        ExternalCompanyResolutionResult result = companyService.getOrCreateExternalCompany("GREENHOUSE", "Vercel", "Vercel");

        assertThat(result.company()).isEqualTo(existing);
        assertThat(result.reusedAfterDuplicate()).isTrue();
    }
}

