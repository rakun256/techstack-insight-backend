package com.emreuslu.techstack.backend.job.entity;

import com.emreuslu.techstack.backend.company.entity.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "jobs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_job_external_id_source",
                        columnNames = {"external_id", "source"}
                )
        }
)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String title;

    @Column(name = "normalized_title")
    private String normalizedTitle;

    @Column(name = "software_relevant", nullable = false)
    private boolean softwareRelevant;

    @Column(name = "role_family")
    private String roleFamily;

    @Column(name = "role_subfamily")
    private String roleSubfamily;

    @Column(nullable = false)
    private String location;

    @Column(name = "location_normalized")
    private String locationNormalized;

    @Column
    private String country;

    @Column(name = "is_remote", nullable = false)
    private boolean remote;

    @Column(name = "is_hybrid", nullable = false)
    private boolean hybrid;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(name = "apply_url", nullable = false)
    private String applyUrl;

    @Column(name = "posted_at", nullable = false)
    private LocalDate postedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}

