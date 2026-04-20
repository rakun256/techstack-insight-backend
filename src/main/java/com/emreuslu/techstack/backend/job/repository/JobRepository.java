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
            select j.location as location, count(j.id) as jobCount
            from Job j
            where j.location is not null and trim(j.location) <> ''
            group by j.location
            order by count(j.id) desc, j.location asc
            """)
    List<LocationTrendProjection> findLocationTrends();
}
