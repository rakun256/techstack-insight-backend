package com.emreuslu.techstack.backend.job.repository;

import com.emreuslu.techstack.backend.job.entity.Job;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByExternalIdAndSource(String externalId, String source);
}

