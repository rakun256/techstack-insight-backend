package com.emreuslu.techstack.backend.analytics.projection;

import java.time.LocalDate;

public interface RoleFamilyTrendProjection {

    LocalDate getDateBucket();

    String getRoleFamily();

    Long getJobCount();
}

