package com.emreuslu.techstack.backend.skill.service;

import com.emreuslu.techstack.backend.skill.entity.Skill;
import com.emreuslu.techstack.backend.skill.entity.SkillAlias;
import com.emreuslu.techstack.backend.skill.repository.SkillAliasRepository;
import com.emreuslu.techstack.backend.skill.repository.SkillRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unused")  // Future skill deduplication and admin APIs
public class SkillAliasService {

    private final SkillAliasRepository skillAliasRepository;
    private final SkillRepository skillRepository;

    public SkillAlias createAlias(Long canonicalSkillId, String aliasName) {
        Optional<Skill> skill = skillRepository.findById(canonicalSkillId);
        if (skill.isEmpty()) {
            throw new IllegalArgumentException("Canonical skill not found with id: " + canonicalSkillId);
        }

        String normalizedAlias = aliasName.trim();
        if (skillAliasRepository.existsByAliasName(normalizedAlias)) {
            throw new IllegalArgumentException("Alias already exists: " + normalizedAlias);
        }

        SkillAlias alias = SkillAlias.builder()
                .canonicalSkill(skill.get())
                .aliasName(normalizedAlias)
                .build();

        return skillAliasRepository.save(alias);
    }

    public Optional<Skill> resolveAlias(String aliasName) {
        return skillAliasRepository.findByAliasName(aliasName.trim())
                .map(SkillAlias::getCanonicalSkill);
    }

    public List<SkillAlias> getAliasesForSkill(Long skillId) {
        return skillAliasRepository.findByCanonicalSkillId(skillId);
    }

    public void seedCommonAliases() {
        // Seed common skill aliases if they don't exist
        seedAliasIfMissing("JavaScript", "js");
        seedAliasIfMissing("JavaScript", "Javascript");
        seedAliasIfMissing("TypeScript", "ts");
        seedAliasIfMissing("TypeScript", "Typescript");
        seedAliasIfMissing("PostgreSQL", "postgres");
        seedAliasIfMissing("PostgreSQL", "Postgres");
        seedAliasIfMissing("Kubernetes", "k8s");
        seedAliasIfMissing("React", "react.js");
        seedAliasIfMissing("React", "reactjs");
        seedAliasIfMissing("Vue", "vue.js");
        seedAliasIfMissing("Vue", "vuejs");
        seedAliasIfMissing("Angular", "angular.js");
        seedAliasIfMissing("Angular", "angularjs");
        seedAliasIfMissing("Node.js", "nodejs");
        seedAliasIfMissing("Node.js", "node");
        seedAliasIfMissing("Python", "python3");
        seedAliasIfMissing("Java", "java8");
        seedAliasIfMissing("MySQL", "mysql");
        seedAliasIfMissing("MongoDB", "mongo");
        seedAliasIfMissing("Redis", "redis");
        seedAliasIfMissing("Docker", "docker");
    }

    private void seedAliasIfMissing(String skillName, String aliasName) {
        if (skillAliasRepository.existsByAliasName(aliasName.trim())) {
            return;
        }

        Optional<Skill> skill = skillRepository.findByNameIgnoreCase(skillName.trim());
        if (skill.isPresent()) {
            try {
                createAlias(skill.get().getId(), aliasName);
                log.info("Created skill alias: {} -> {}", aliasName, skillName);
            } catch (Exception e) {
                log.debug("Could not create alias {} -> {}: {}", aliasName, skillName, e.getMessage());
            }
        }
    }
}

