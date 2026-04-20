package com.emreuslu.techstack.backend.jobskill.service;

import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.jobskill.entity.JobSkill;
import com.emreuslu.techstack.backend.jobskill.repository.JobSkillRepository;
import com.emreuslu.techstack.backend.skill.entity.Skill;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobSkillService {

    private final JobSkillRepository jobSkillRepository;

    @Transactional
    public JobSkill linkSkillToJob(Job job, Skill skill) {
        Objects.requireNonNull(job, "job must not be null");
        Objects.requireNonNull(skill, "skill must not be null");

        return jobSkillRepository.findByJobAndSkill(job, skill)
                .orElseGet(() -> jobSkillRepository.save(JobSkill.builder()
                        .job(job)
                        .skill(skill)
                        .build()));
    }

    @Transactional
    public List<JobSkill> linkSkillsToJob(Job job, Collection<Skill> skills) {
        Objects.requireNonNull(job, "job must not be null");

        if (skills == null || skills.isEmpty()) {
            return List.of();
        }

        Set<Skill> uniqueSkills = new LinkedHashSet<>(skills);
        List<JobSkill> linkedJobSkills = new ArrayList<>();

        for (Skill skill : uniqueSkills) {
            if (skill != null) {
                linkedJobSkills.add(linkSkillToJob(job, skill));
            }
        }

        return linkedJobSkills;
    }

    @Transactional(readOnly = true)
    public boolean existsByJobAndSkill(Job job, Skill skill) {
        Objects.requireNonNull(job, "job must not be null");
        Objects.requireNonNull(skill, "skill must not be null");
        return jobSkillRepository.existsByJobAndSkill(job, skill);
    }
}

