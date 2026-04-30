package com.emreuslu.techstack.backend.analytics.projection;

import java.util.UUID;

public interface CompanyRoleDistributionProjection {

    UUID getCompanyId();

    String getCompanyName();

    String getRoleFamily();

    Long getJobCount();
}

