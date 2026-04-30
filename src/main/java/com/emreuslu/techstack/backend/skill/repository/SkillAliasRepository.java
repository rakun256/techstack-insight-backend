package com.emreuslu.techstack.backend.skill.repository;

import com.emreuslu.techstack.backend.skill.entity.SkillAlias;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillAliasRepository extends JpaRepository<SkillAlias, Long> {

    Optional<SkillAlias> findByAliasName(String aliasName);

    List<SkillAlias> findByCanonicalSkillId(Long canonicalSkillId);

    boolean existsByAliasName(String aliasName);
}

