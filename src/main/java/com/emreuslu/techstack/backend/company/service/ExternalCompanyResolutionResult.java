package com.emreuslu.techstack.backend.company.service;

import com.emreuslu.techstack.backend.company.entity.Company;

public record ExternalCompanyResolutionResult(
        Company company,
        boolean reusedAfterDuplicate
) {
}

