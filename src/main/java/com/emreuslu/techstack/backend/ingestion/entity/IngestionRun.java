package com.emreuslu.techstack.backend.ingestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "ingestion_runs",
        indexes = {
                @Index(name = "idx_ingestion_runs_source_token_started", columnList = "source,token,started_at"),
                @Index(name = "idx_ingestion_runs_source_started", columnList = "source,started_at"),
                @Index(name = "idx_ingestion_runs_status", columnList = "status")
        }
)
public class IngestionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(nullable = false, length = 100)
    private String token;

    @Column(name = "trigger_type", nullable = false, length = 20)
    private String triggerType;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "fetched_count", nullable = false)
    private int fetchedCount;

    @Column(name = "inserted_count", nullable = false)
    private int insertedCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    @Column(name = "software_relevant_count", nullable = false)
    private int softwareRelevantCount;

    @Column(name = "extracted_skills_count", nullable = false)
    private int extractedSkillsCount;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Column(name = "company_reused_after_duplicate_count", nullable = false)
    private int companyReusedAfterDuplicateCount;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "run_duration_ms")
    private long runDurationMs;
}

