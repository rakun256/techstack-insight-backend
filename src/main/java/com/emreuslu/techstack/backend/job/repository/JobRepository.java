package com.emreuslu.techstack.backend.job.repository;

import com.emreuslu.techstack.backend.analytics.projection.CountrySkillTrendProjection;
import com.emreuslu.techstack.backend.analytics.projection.LocationTrendProjection;
import com.emreuslu.techstack.backend.analytics.projection.RoleFamilyTrendProjection;
import com.emreuslu.techstack.backend.analytics.projection.TrendingSkillProjection;
import com.emreuslu.techstack.backend.analytics.projection.WorkModeTrendProjection;
import com.emreuslu.techstack.backend.job.entity.Job;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Time-series analytics queries

    @Query("""
            select CAST(j.postedAt as LocalDate) as dateBucket, s.name as skillName, count(distinct j.id) as jobCount
            from JobSkill js
            join js.job j
            join js.skill s
            where j.softwareRelevant = true
              and j.postedAt >= :fromDate
              and j.postedAt <= :toDate
            group by CAST(j.postedAt as LocalDate), s.name
            order by CAST(j.postedAt as LocalDate) desc, count(distinct j.id) desc, s.name asc
            """)
    List<TrendingSkillProjection> findTrendingSkills(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            select CAST(j.postedAt as LocalDate) as dateBucket, 
                   coalesce(j.roleFamily, 'UNKNOWN') as roleFamily, 
                   count(j.id) as jobCount
            from Job j
            where j.softwareRelevant = true
              and j.postedAt >= :fromDate
              and j.postedAt <= :toDate
            group by CAST(j.postedAt as LocalDate), coalesce(j.roleFamily, 'UNKNOWN')
            order by CAST(j.postedAt as LocalDate) desc, count(j.id) desc
            """)
    List<RoleFamilyTrendProjection> findRoleFamilyTrends(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            select CAST(j.postedAt as LocalDate) as dateBucket, s.name as skillName, count(distinct j.id) as jobCount
            from JobSkill js
            join js.job j
            join js.skill s
            where j.softwareRelevant = true
              and j.country = :country
              and j.postedAt >= :fromDate
              and j.postedAt <= :toDate
            group by CAST(j.postedAt as LocalDate), s.name
            order by CAST(j.postedAt as LocalDate) desc, count(distinct j.id) desc, s.name asc
            """)
    List<CountrySkillTrendProjection> findCountrySkillTrends(
            @Param("country") String country,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            select CAST(j.postedAt as LocalDate) as dateBucket,
                   case
                      when j.remote = true then 'REMOTE'
                      when j.hybrid = true then 'HYBRID'
                      else 'ONSITE_OR_UNSPECIFIED'
                   end as workMode,
                   count(j.id) as jobCount
            from Job j
            where j.softwareRelevant = true
              and j.postedAt >= :fromDate
              and j.postedAt <= :toDate
            group by CAST(j.postedAt as LocalDate),
                     case
                        when j.remote = true then 'REMOTE'
                        when j.hybrid = true then 'HYBRID'
                        else 'ONSITE_OR_UNSPECIFIED'
                     end
            order by CAST(j.postedAt as LocalDate) desc, count(j.id) desc
            """)
    List<WorkModeTrendProjection> findWorkModeTrends(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // Job filtering queries

    @Query("""
            select j from Job j
            where j.softwareRelevant = true
              and (:roleFamily is null or j.roleFamily = :roleFamily)
              and (:country is null or j.country = :country)
              and (:remote is null or j.remote = :remote)
              and (:hybrid is null or j.hybrid = :hybrid)
              and (:companyId is null or j.company.id = :companyId)
              and (:source is null or j.source = :source)
              and (:postedAtFrom is null or j.postedAt >= :postedAtFrom)
              and (:postedAtTo is null or j.postedAt <= :postedAtTo)
              and (:titleQuery is null or j.normalizedTitle ilike concat('%', :titleQuery, '%'))
            order by j.postedAt desc
            """)
    List<Job> findByFilters(
            @Param("roleFamily") String roleFamily,
            @Param("country") String country,
            @Param("remote") Boolean remote,
            @Param("hybrid") Boolean hybrid,
            @Param("companyId") UUID companyId,
            @Param("source") String source,
            @Param("postedAtFrom") LocalDate postedAtFrom,
            @Param("postedAtTo") LocalDate postedAtTo,
            @Param("titleQuery") String titleQuery
    );
}
