package com.emreuslu.techstack.backend.job.entity;

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
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "title_aliases",
        indexes = {
                @Index(name = "idx_title_aliases_raw_pattern", columnList = "raw_title_pattern"),
                @Index(name = "idx_title_aliases_role_family", columnList = "role_family"),
                @Index(name = "idx_title_aliases_active", columnList = "active")
        }
)
public class TitleAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_title_pattern", nullable = false, columnDefinition = "TEXT")
    private String rawTitlePattern;

    @Column(name = "normalized_title", nullable = false, length = 255)
    private String normalizedTitle;

    @Column(name = "role_family", length = 100)
    private String roleFamily;

    @Column(name = "role_subfamily", length = 100)
    private String roleSubfamily;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

