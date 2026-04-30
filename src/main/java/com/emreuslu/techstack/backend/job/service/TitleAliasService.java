package com.emreuslu.techstack.backend.job.service;

import com.emreuslu.techstack.backend.job.entity.TitleAlias;
import com.emreuslu.techstack.backend.job.repository.TitleAliasRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unused")  // Future pattern management and advanced role classification
public class TitleAliasService {

    private final TitleAliasRepository titleAliasRepository;

    public TitleAlias createTitleAlias(
            String rawTitlePattern,
            String normalizedTitle,
            String roleFamily,
            String roleSubfamily
    ) {
        TitleAlias alias = TitleAlias.builder()
                .rawTitlePattern(rawTitlePattern)
                .normalizedTitle(normalizedTitle)
                .roleFamily(roleFamily)
                .roleSubfamily(roleSubfamily)
                .active(true)
                .build();

        return titleAliasRepository.save(alias);
    }

    public List<TitleAlias> getActiveTitleAliases() {
        return titleAliasRepository.findByActiveTrue();
    }

    public List<TitleAlias> getTitleAliasesByRoleFamily(String roleFamily) {
        return titleAliasRepository.findByRoleFamily(roleFamily);
    }

    public List<TitleAlias> getTitleAliasesByRoleFamilyAndSubfamily(String roleFamily, String roleSubfamily) {
        return titleAliasRepository.findByRoleFamilyAndRoleSubfamily(roleFamily, roleSubfamily);
    }

    public void seedCommonTitleAliases() {
        // Seed common title patterns for normalization
        seedTitleAliasIfMissing("Backend Engineer", "Backend Engineer", "BACKEND", "BACKEND");
        seedTitleAliasIfMissing("Back-end Engineer", "Backend Engineer", "BACKEND", "BACKEND");
        seedTitleAliasIfMissing("Frontend Engineer", "Frontend Engineer", "FRONTEND", "FRONTEND");
        seedTitleAliasIfMissing("Front-end Engineer", "Frontend Engineer", "FRONTEND", "FRONTEND");
        seedTitleAliasIfMissing("Full Stack Engineer", "Full Stack Engineer", "FULLSTACK", "FULLSTACK");
        seedTitleAliasIfMissing("Fullstack Engineer", "Full Stack Engineer", "FULLSTACK", "FULLSTACK");
        seedTitleAliasIfMissing("DevOps Engineer", "DevOps Engineer", "DEVOPS", "DEVOPS");
        seedTitleAliasIfMissing("Data Scientist", "Data Scientist", "DATA", "SCIENTIST");
        seedTitleAliasIfMissing("Data Engineer", "Data Engineer", "DATA", "ENGINEER");
        seedTitleAliasIfMissing("Machine Learning Engineer", "ML Engineer", "DATA", "ML");
        seedTitleAliasIfMissing("ML Engineer", "ML Engineer", "DATA", "ML");
        seedTitleAliasIfMissing("Software Engineer", "Software Engineer", "GENERAL", "GENERAL");
        seedTitleAliasIfMissing("QA Engineer", "QA Engineer", "QA", "QA");
        seedTitleAliasIfMissing("Quality Assurance Engineer", "QA Engineer", "QA", "QA");
        seedTitleAliasIfMissing("Solutions Architect", "Solutions Architect", "ARCHITECTURE", "SOLUTIONS");
        seedTitleAliasIfMissing("Enterprise Architect", "Enterprise Architect", "ARCHITECTURE", "ENTERPRISE");
        seedTitleAliasIfMissing("Technical Lead", "Technical Lead", "LEADERSHIP", "LEAD");
        seedTitleAliasIfMissing("Tech Lead", "Technical Lead", "LEADERSHIP", "LEAD");
        seedTitleAliasIfMissing("Engineering Manager", "Engineering Manager", "MANAGEMENT", "MANAGEMENT");
        seedTitleAliasIfMissing("Product Manager", "Product Manager", "PRODUCT", "MANAGEMENT");
    }

    private void seedTitleAliasIfMissing(String pattern, String normalized, String family, String subfamily) {
        List<TitleAlias> existing = titleAliasRepository.findByActiveTrue();
        boolean exists = existing.stream()
                .anyMatch(a -> a.getRawTitlePattern().equalsIgnoreCase(pattern));

        if (!exists) {
            try {
                createTitleAlias(pattern, normalized, family, subfamily);
                log.info("Created title alias: {} -> {} ({}/{})", pattern, normalized, family, subfamily);
            } catch (Exception e) {
                log.debug("Could not create title alias {}: {}", pattern, e.getMessage());
            }
        }
    }
}

