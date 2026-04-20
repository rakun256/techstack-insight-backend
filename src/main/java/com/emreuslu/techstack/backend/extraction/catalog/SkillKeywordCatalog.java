package com.emreuslu.techstack.backend.extraction.catalog;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SkillKeywordCatalog {

    private final List<SkillKeywordEntry> entries = List.of(
            new SkillKeywordEntry("Java", List.of("java")),
            new SkillKeywordEntry("Spring Boot", List.of("spring boot", "springboot")),
            new SkillKeywordEntry("PostgreSQL", List.of("postgresql", "postgres")),
            new SkillKeywordEntry("SQL", List.of("sql")),
            new SkillKeywordEntry("Docker", List.of("docker")),
            new SkillKeywordEntry("Kubernetes", List.of("kubernetes", "k8s")),
            new SkillKeywordEntry("React", List.of("react", "reactjs")),
            new SkillKeywordEntry("TypeScript", List.of("typescript", "ts")),
            new SkillKeywordEntry("JavaScript", List.of("javascript", "js")),
            new SkillKeywordEntry("Python", List.of("python")),
            new SkillKeywordEntry("Git", List.of("git")),
            new SkillKeywordEntry("REST API", List.of("rest api", "restful api")),
            new SkillKeywordEntry("AWS", List.of("aws", "amazon web services")),
            new SkillKeywordEntry("Redis", List.of("redis"))
    );

    public List<SkillKeywordEntry> entries() {
        return entries;
    }

    public record SkillKeywordEntry(
            String canonicalSkillName,
            List<String> keywords
    ) {
    }
}

