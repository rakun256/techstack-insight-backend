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
@ConfigurationProperties(prefix = "app.ingestion")
public class IngestionProperties {

    private boolean runOnStartup = false;
    private boolean schedulerEnabled = false;
    private String schedulerCron = "0 0 3 * * *";
    private List<Source> sources = new ArrayList<>();


    @Getter
    @Setter
    public static class Source {
        private String type;
        private String token;
        private boolean enabled = true;
    }
}

