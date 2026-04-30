package com.emreuslu.techstack.backend.analytics.projection;

import java.time.LocalDate;

public interface CountrySkillTrendProjection {

    LocalDate getDateBucket();

    String getSkillName();

    Long getJobCount();
}

