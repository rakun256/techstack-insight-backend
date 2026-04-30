package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.entity.RawJobPayload;
import com.emreuslu.techstack.backend.ingestion.repository.RawJobPayloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RawJobPayloadCaptureTest {

    @Mock
    private RawJobPayloadRepository rawJobPayloadRepository;

    @InjectMocks
    private RawJobPayloadService rawJobPayloadService;

    private String testPayloadJson;

    @BeforeEach
    void setUp() {
        testPayloadJson = "{\"id\": 123, \"title\": \"Backend Engineer\", \"company\": \"TechCorp\"}";
    }

    @Test
    void saveRawPayload_persistsJsonWithChecksum() {
        ArgumentCaptor<RawJobPayload> captor = ArgumentCaptor.forClass(RawJobPayload.class);
        when(rawJobPayloadRepository.save(any(RawJobPayload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        rawJobPayloadService.saveRawPayload(
                "GREENHOUSE",
                "job-123",
                "board-token",
                testPayloadJson
        );

        verify(rawJobPayloadRepository, times(1)).save(captor.capture());

        RawJobPayload saved = captor.getValue();
        assertEquals("GREENHOUSE", saved.getSource());
        assertEquals("job-123", saved.getExternalJobId());
        assertEquals(testPayloadJson, saved.getPayloadJson());
        assertNotNull(saved.getChecksum());
        assertFalse(saved.getChecksum().isEmpty());
    }

    @Test
    void saveRawPayload_calculatesValidChecksum() {
        when(rawJobPayloadRepository.save(any(RawJobPayload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RawJobPayload payload1 = rawJobPayloadService.saveRawPayload(
                "GREENHOUSE",
                "job-1",
                null,
                testPayloadJson
        );

        RawJobPayload payload2 = rawJobPayloadService.saveRawPayload(
                "GREENHOUSE",
                "job-2",
                null,
                testPayloadJson
        );

        // Same JSON should produce same checksum
        assertEquals(payload1.getChecksum(), payload2.getChecksum());
    }

    @Test
    void saveRawPayload_withParseStatus() {
        when(rawJobPayloadRepository.save(any(RawJobPayload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RawJobPayload result = rawJobPayloadService.saveRawPayload(
                "LEVER",
                "posting-456",
                null,
                testPayloadJson,
                "SUCCESS"
        );

        assertEquals("SUCCESS", result.getParseStatus());
    }

    @Test
    void differentPayloads_produceDifferentChecksums() {
        when(rawJobPayloadRepository.save(any(RawJobPayload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RawJobPayload payload1 = rawJobPayloadService.saveRawPayload(
                "GREENHOUSE",
                "job-1",
                null,
                "{\"id\": 1}"
        );

        RawJobPayload payload2 = rawJobPayloadService.saveRawPayload(
                "GREENHOUSE",
                "job-2",
                null,
                "{\"id\": 2}"
        );

        assertNotEquals(payload1.getChecksum(), payload2.getChecksum());
    }
}

