package com.emreuslu.techstack.backend.jobskill.repository;

import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.jobskill.entity.JobSkill;
import com.emreuslu.techstack.backend.skill.entity.Skill;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {

    boolean existsByJobAndSkill(Job job, Skill skill);

    Optional<JobSkill> findByJobAndSkill(Job job, Skill skill);
}

