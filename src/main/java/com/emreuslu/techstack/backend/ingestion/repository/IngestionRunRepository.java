package com.emreuslu.techstack.backend.ingestion.repository;

import com.emreuslu.techstack.backend.ingestion.entity.IngestionRun;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IngestionRunRepository extends JpaRepository<IngestionRun, Long> {

    // ...existing code...

    List<IngestionRun> findBySourceOrderByStartedAtDesc(String source);

    List<IngestionRun> findByStatus(String status);

    @Query("SELECT ir FROM IngestionRun ir WHERE ir.source = :source AND ir.token = :token AND ir.startedAt >= :since ORDER BY ir.startedAt DESC")
    List<IngestionRun> findRecentRuns(@Param("source") String source, @Param("token") String token, @Param("since") Instant since);
}

