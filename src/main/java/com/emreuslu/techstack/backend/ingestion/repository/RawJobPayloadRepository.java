package com.emreuslu.techstack.backend.ingestion.repository;

import com.emreuslu.techstack.backend.ingestion.entity.RawJobPayload;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawJobPayloadRepository extends JpaRepository<RawJobPayload, Long> {

    Optional<RawJobPayload> findBySourceAndExternalJobId(String source, String externalJobId);

    Optional<RawJobPayload> findByChecksum(String checksum);

    long countBySource(String source);
}

