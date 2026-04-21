package com.emreuslu.techstack.backend.ingestion.controller;

import com.emreuslu.techstack.backend.ingestion.dto.IngestionRunStatsDto;
import com.emreuslu.techstack.backend.ingestion.dto.IngestionProperties;
import com.emreuslu.techstack.backend.ingestion.service.JobIngestionFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ingestion")
@RequiredArgsConstructor
@Tag(name = "Ingestion Admin", description = "Manual ingestion triggers")
public class IngestionAdminController {

    private final JobIngestionFacade jobIngestionFacade;
    private final IngestionProperties ingestionProperties;

    @PostMapping("/run")
    @Operation(summary = "Run ingestion for all enabled configured sources")
    public ResponseEntity<List<IngestionRunStatsDto>> runAllEnabledSources() {
        return ResponseEntity.ok(jobIngestionFacade.ingestAllConfiguredSources(ingestionProperties.getSources()));
    }

    @PostMapping("/run-source")
    @Operation(summary = "Run ingestion for one source type and token")
    public ResponseEntity<IngestionRunStatsDto> runOneSource(
            @RequestParam String type,
            @RequestParam String token
    ) {
        return ResponseEntity.ok(jobIngestionFacade.ingestConfiguredSource(type, token));
    }
}

