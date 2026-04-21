package com.emreuslu.techstack.backend.job.repository;

import com.emreuslu.techstack.backend.analytics.projection.LocationTrendProjection;
import com.emreuslu.techstack.backend.job.entity.Job;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByExternalIdAndSource(String externalId, String source);

    @Query("""
            select j.locationNormalized as location, count(j.id) as jobCount
            from Job j
            where j.softwareRelevant = true
              and j.locationNormalized is not null
              and trim(j.locationNormalized) <> ''
            group by j.locationNormalized
            order by count(j.id) desc, j.locationNormalized asc
            """)
    List<LocationTrendProjection> findLocationTrends();

    @Query("""
            select coalesce(j.roleFamily, 'UNKNOWN') as roleFamily, count(j.id) as jobCount
            from Job j
            where j.softwareRelevant = true
            group by coalesce(j.roleFamily, 'UNKNOWN')
            order by count(j.id) desc
            """)
    List<Object[]> findRoleFamilyDistribution();

    @Query("""
            select case
                     when j.remote = true then 'REMOTE'
                     when j.hybrid = true then 'HYBRID'
                     else 'ONSITE_OR_UNSPECIFIED'
                   end as workMode,
                   count(j.id) as jobCount
            from Job j
            where j.softwareRelevant = true
            group by case
                       when j.remote = true then 'REMOTE'
                       when j.hybrid = true then 'HYBRID'
                       else 'ONSITE_OR_UNSPECIFIED'
                     end
            order by count(j.id) desc
            """)
    List<Object[]> findWorkModeDistribution();

    @Query("""
            select coalesce(j.country, 'UNKNOWN') as country, count(j.id) as jobCount
            from Job j
            where j.softwareRelevant = true
            group by coalesce(j.country, 'UNKNOWN')
            order by count(j.id) desc
            """)
    List<Object[]> findCountryDistribution();
}
