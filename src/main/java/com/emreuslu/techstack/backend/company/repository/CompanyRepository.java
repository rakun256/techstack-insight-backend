package com.emreuslu.techstack.backend.company.repository;

import com.emreuslu.techstack.backend.company.entity.Company;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByExternalSourceAndExternalCompanyId(String externalSource, String externalCompanyId);

    Optional<Company> findByNameIgnoreCase(String name);
}
