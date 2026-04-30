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
        name = "raw_job_payloads",
        indexes = {
                @Index(name = "idx_raw_job_payloads_source_external_id", columnList = "source,external_job_id"),
                @Index(name = "idx_raw_job_payloads_source_checksum", columnList = "source,checksum"),
                @Index(name = "idx_raw_job_payloads_fetched_at", columnList = "fetched_at")
        }
)
public class RawJobPayload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "external_job_id", nullable = false, length = 255)
    private String externalJobId;

    @Column(name = "source_token", length = 255)
    private String sourceToken;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @CreationTimestamp
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private Instant fetchedAt;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "parse_status", length = 20)
    private String parseStatus;
}

