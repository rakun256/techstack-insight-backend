package com.emreuslu.techstack.backend.analytics.projection;

import java.time.LocalDate;

public interface WorkModeTrendProjection {

    LocalDate getDateBucket();

    String getWorkMode();

    Long getJobCount();
}

