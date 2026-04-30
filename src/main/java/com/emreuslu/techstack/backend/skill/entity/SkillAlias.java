package com.emreuslu.techstack.backend.skill.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        name = "skill_aliases",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_skill_aliases_canonical_alias",
                        columnNames = {"canonical_skill_id", "alias_name"}
                )
        },
        indexes = {
                @Index(name = "idx_skill_aliases_alias_name", columnList = "alias_name"),
                @Index(name = "idx_skill_aliases_canonical_skill_id", columnList = "canonical_skill_id")
        }
)
public class SkillAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canonical_skill_id", nullable = false)
    private Skill canonicalSkill;

    @Column(name = "alias_name", nullable = false, length = 255)
    private String aliasName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

