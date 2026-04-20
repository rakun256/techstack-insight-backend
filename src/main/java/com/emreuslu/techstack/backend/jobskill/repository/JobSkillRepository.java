package com.emreuslu.techstack.backend.jobskill.repository;

import com.emreuslu.techstack.backend.analytics.projection.RoleSkillDistributionProjection;
import com.emreuslu.techstack.backend.analytics.projection.TopSkillProjection;
import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.jobskill.entity.JobSkill;
import com.emreuslu.techstack.backend.skill.entity.Skill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {

    boolean existsByJobAndSkill(Job job, Skill skill);

    Optional<JobSkill> findByJobAndSkill(Job job, Skill skill);

    @Query("""
            select s.name as skillName, count(distinct j.id) as jobCount
            from JobSkill js
            join js.skill s
            join js.job j
            group by s.name
            order by count(distinct j.id) desc, s.name asc
            """)
    List<TopSkillProjection> findTopSkillCounts();

    @Query("""
            select j.title as jobTitle, s.name as skillName, count(distinct j.id) as jobCount
            from JobSkill js
            join js.job j
            join js.skill s
            group by j.title, s.name
            order by count(distinct j.id) desc, j.title asc, s.name asc
            """)
    List<RoleSkillDistributionProjection> findRoleSkillDistribution();
}
