package com.emreuslu.techstack.backend.ingestion.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ingestion")
public class IngestionProperties {

    private Scheduler scheduler = new Scheduler();
    private List<Source> sources = new ArrayList<>();

    @Getter
    @Setter
    public static class Scheduler {
        private long fixedDelayMs = 3600000;
        private long initialDelayMs = 30000;
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class Source {
        private String type;
        private String token;
        private boolean enabled = true;
    }
}

