package com.emreuslu.techstack.backend.analytics.controller;

import com.emreuslu.techstack.backend.analytics.dto.LocationTrendResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.RoleSkillDistributionResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.TopSkillResponseDto;
import com.emreuslu.techstack.backend.analytics.service.AnalyticsService;
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
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-skills")
    public ResponseEntity<List<TopSkillResponseDto>> getTopSkills(
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(analyticsService.getTopSkills(limit));
    }

    @GetMapping("/location-trends")
    public ResponseEntity<List<LocationTrendResponseDto>> getLocationTrends() {
        return ResponseEntity.ok(analyticsService.getLocationTrends());
    }

    @GetMapping("/role-skill-distribution")
    public ResponseEntity<List<RoleSkillDistributionResponseDto>> getRoleSkillDistribution() {
        return ResponseEntity.ok(analyticsService.getRoleSkillDistribution());
    }
}

