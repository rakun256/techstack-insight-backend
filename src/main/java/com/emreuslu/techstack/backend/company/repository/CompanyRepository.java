package com.emreuslu.techstack.backend.company.repository;

import com.emreuslu.techstack.backend.company.entity.Company;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
}

