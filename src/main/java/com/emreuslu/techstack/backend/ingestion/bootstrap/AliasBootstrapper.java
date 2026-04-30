package com.emreuslu.techstack.backend.ingestion.bootstrap;

import com.emreuslu.techstack.backend.job.service.TitleAliasService;
import com.emreuslu.techstack.backend.skill.service.SkillAliasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AliasBootstrapper {

    private final SkillAliasService skillAliasService;
    private final TitleAliasService titleAliasService;

    @EventListener(ApplicationReadyEvent.class)
    public void seedAliases() {
        try {
            log.info("Bootstrapping skill and title aliases...");
            skillAliasService.seedCommonAliases();
            titleAliasService.seedCommonTitleAliases();
            log.info("Alias bootstrapping completed successfully");
        } catch (Exception e) {
            log.warn("Error during alias bootstrapping: {}", e.getMessage(), e);
        }
    }
}

