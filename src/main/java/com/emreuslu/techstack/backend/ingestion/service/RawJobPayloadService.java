package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.entity.RawJobPayload;
import com.emreuslu.techstack.backend.ingestion.repository.RawJobPayloadRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unused")  // Future queries for replay/audit
public class RawJobPayloadService {

    private final RawJobPayloadRepository rawJobPayloadRepository;

    public RawJobPayload saveRawPayload(String source, String externalJobId, String sourceToken, String jsonPayload) {
        return saveRawPayload(source, externalJobId, sourceToken, jsonPayload, null);
    }

    public RawJobPayload saveRawPayload(
            String source,
            String externalJobId,
            String sourceToken,
            String jsonPayload,
            String parseStatus
    ) {
        String checksum = calculateChecksum(jsonPayload);

        RawJobPayload payload = RawJobPayload.builder()
                .source(source)
                .externalJobId(externalJobId)
                .sourceToken(sourceToken)
                .payloadJson(jsonPayload)
                .checksum(checksum)
                .parseStatus(parseStatus != null ? parseStatus : "SUCCESS")
                .build();

        return rawJobPayloadRepository.save(payload);
    }

    public Optional<RawJobPayload> findBySourceAndExternalJobId(String source, String externalJobId) {
        return rawJobPayloadRepository.findBySourceAndExternalJobId(source, externalJobId);
    }

    public Optional<RawJobPayload> findByChecksum(String checksum) {
        return rawJobPayloadRepository.findByChecksum(checksum);
    }

    public long countBySource(String source) {
        return rawJobPayloadRepository.countBySource(source);
    }

    private String calculateChecksum(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 not available, returning null checksum", e);
            return null;
        }
    }
}

