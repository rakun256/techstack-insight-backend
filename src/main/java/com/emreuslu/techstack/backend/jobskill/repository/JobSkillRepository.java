package com.emreuslu.techstack.backend.jobskill.repository;

import com.emreuslu.techstack.backend.analytics.projection.CompanyRoleDistributionProjection;
import com.emreuslu.techstack.backend.analytics.projection.CompanyTopSkillsProjection;
import com.emreuslu.techstack.backend.analytics.projection.CompanyWorkModeDistributionProjection;
import com.emreuslu.techstack.backend.analytics.projection.RoleSkillDistributionProjection;
import com.emreuslu.techstack.backend.analytics.projection.TopSkillProjection;
import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.jobskill.entity.JobSkill;
import com.emreuslu.techstack.backend.skill.entity.Skill;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Company analytics queries

    @Query("""
            select j.company.id as companyId,
                   j.company.name as companyName,
                   s.name as skillName,
                   count(distinct j.id) as jobCount
            from JobSkill js
            join js.job j
            join js.skill s
            where j.company.id = :companyId
              and j.softwareRelevant = true
            group by j.company.id, j.company.name, s.name
            order by count(distinct j.id) desc, s.name asc
            """)
    List<CompanyTopSkillsProjection> findTopSkillsByCompany(@Param("companyId") UUID companyId);

    @Query("""
            select j.company.id as companyId,
                   j.company.name as companyName,
                   coalesce(j.roleFamily, 'UNKNOWN') as roleFamily,
                   count(j.id) as jobCount
            from Job j
            where j.company.id = :companyId
              and j.softwareRelevant = true
            group by j.company.id, j.company.name, coalesce(j.roleFamily, 'UNKNOWN')
            order by count(j.id) desc
            """)
    List<CompanyRoleDistributionProjection> findRoleDistributionByCompany(@Param("companyId") UUID companyId);

    @Query("""
            select j.company.id as companyId,
                   j.company.name as companyName,
                   case
                      when j.remote = true then 'REMOTE'
                      when j.hybrid = true then 'HYBRID'
                      else 'ONSITE_OR_UNSPECIFIED'
                   end as workMode,
                   count(j.id) as jobCount
            from Job j
            where j.company.id = :companyId
              and j.softwareRelevant = true
            group by j.company.id, j.company.name,
                     case
                        when j.remote = true then 'REMOTE'
                        when j.hybrid = true then 'HYBRID'
                        else 'ONSITE_OR_UNSPECIFIED'
                     end
            order by count(j.id) desc
            """)
    List<CompanyWorkModeDistributionProjection> findWorkModeDistributionByCompany(@Param("companyId") UUID companyId);

    // Find jobs by skill for filtering

    @Query("""
            select distinct j from JobSkill js
            join js.job j
            where js.skill.id = :skillId
              and j.softwareRelevant = true
            """)
    List<Job> findJobsBySkillId(@Param("skillId") Long skillId);

    @Query("""
            select distinct j from JobSkill js
            join js.job j
            join js.skill s
            where s.name = :skillName
              and j.softwareRelevant = true
            """)
    List<Job> findJobsBySkillName(@Param("skillName") String skillName);
}
