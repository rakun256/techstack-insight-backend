package com.emreuslu.techstack.backend.analytics.projection;

import java.util.UUID;

public interface CompanyWorkModeDistributionProjection {

    UUID getCompanyId();

    String getCompanyName();

    String getWorkMode();

    Long getJobCount();
}

