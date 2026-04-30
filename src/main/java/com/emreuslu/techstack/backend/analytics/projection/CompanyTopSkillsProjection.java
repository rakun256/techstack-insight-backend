package com.emreuslu.techstack.backend.analytics.projection;

import java.util.UUID;

public interface CompanyTopSkillsProjection {

    UUID getCompanyId();

    String getCompanyName();

    String getSkillName();

    Long getJobCount();
}

