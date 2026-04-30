package com.emreuslu.techstack.backend.job.repository;

import com.emreuslu.techstack.backend.job.entity.TitleAlias;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleAliasRepository extends JpaRepository<TitleAlias, Long> {

    List<TitleAlias> findByActiveTrue();

    List<TitleAlias> findByRoleFamily(String roleFamily);

    List<TitleAlias> findByRoleFamilyAndRoleSubfamily(String roleFamily, String roleSubfamily);
}

