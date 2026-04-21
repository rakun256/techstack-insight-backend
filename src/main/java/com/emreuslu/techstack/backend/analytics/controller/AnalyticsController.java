package com.emreuslu.techstack.backend.analytics.controller;

import com.emreuslu.techstack.backend.analytics.dto.LocationTrendResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.RoleSkillDistributionResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.TopSkillResponseDto;
import com.emreuslu.techstack.backend.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Read-only dashboard analytics endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-skills")
    @Operation(summary = "Get top skills ranked by distinct job count", description = "Optional limit parameter truncates the ranked list")
    public ResponseEntity<List<TopSkillResponseDto>> getTopSkills(
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(analyticsService.getTopSkills(limit));
    }

    @GetMapping("/location-trends")
    @Operation(summary = "Get location trends grouped by location", description = "Read-only aggregation of job counts per location")
    public ResponseEntity<List<LocationTrendResponseDto>> getLocationTrends() {
        return ResponseEntity.ok(analyticsService.getLocationTrends());
    }

    @GetMapping("/role-skill-distribution")
    @Operation(summary = "Get role and skill distribution", description = "Read-only aggregation grouped by job title and skill")
    public ResponseEntity<List<RoleSkillDistributionResponseDto>> getRoleSkillDistribution() {
        return ResponseEntity.ok(analyticsService.getRoleSkillDistribution());
    }
}

