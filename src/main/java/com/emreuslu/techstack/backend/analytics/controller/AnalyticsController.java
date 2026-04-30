package com.emreuslu.techstack.backend.analytics.controller;

import com.emreuslu.techstack.backend.analytics.dto.CompanyComparisonResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.CompanyRoleDistributionResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.CompanyTopSkillsResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.CompanyWorkModeDistributionResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.CountrySkillTrendResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.LocationTrendResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.RoleFamilyTrendResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.RoleSkillDistributionResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.TopSkillResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.TrendingSkillResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.WorkModeTrendResponseDto;
import com.emreuslu.techstack.backend.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
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

    // Time-series analytics endpoints

    @GetMapping("/trending-skills")
    @Operation(
            summary = "Get trending skills over time",
            description = "Returns top skills from jobs posted in the last X days. " +
                    "Note: Trends are based on current job postings' postedAt dates, not historical snapshots. " +
                    "Supports days: 7, 30, 90"
    )
    public ResponseEntity<List<TrendingSkillResponseDto>> getTrendingSkills(
            @RequestParam(defaultValue = "30") Integer days
    ) {
        return ResponseEntity.ok(analyticsService.getTrendingSkills(days));
    }

    @GetMapping("/role-family-trends")
    @Operation(
            summary = "Get role family trends over time",
            description = "Shows how role families trend in jobs posted over the selected period. " +
                    "Based on postedAt dates of current jobs (not historical snapshots)."
    )
    public ResponseEntity<List<RoleFamilyTrendResponseDto>> getRoleFamilyTrends(
            @RequestParam(defaultValue = "30") Integer days
    ) {
        return ResponseEntity.ok(analyticsService.getRoleFamilyTrends(days));
    }

    @GetMapping("/country-skill-trends")
    @Operation(
            summary = "Get country-specific technology trends",
            description = "Shows trending skills for a specific country based on jobs posted in the period. " +
                    "Based on postedAt dates (not historical snapshots)."
    )
    public ResponseEntity<List<CountrySkillTrendResponseDto>> getCountrySkillTrends(
            @RequestParam String country,
            @RequestParam(defaultValue = "30") Integer days
    ) {
        return ResponseEntity.ok(analyticsService.getCountrySkillTrends(country, days));
    }

    @GetMapping("/work-mode-trends")
    @Operation(
            summary = "Get work mode distribution trends",
            description = "Shows remote/hybrid/onsite distribution trends in jobs posted over the period. " +
                    "Based on postedAt dates (not historical snapshots)."
    )
    public ResponseEntity<List<WorkModeTrendResponseDto>> getWorkModeTrends(
            @RequestParam(defaultValue = "30") Integer days
    ) {
        return ResponseEntity.ok(analyticsService.getWorkModeTrends(days));
    }

    // Company analytics endpoints

    @GetMapping("/company-top-skills")
    @Operation(summary = "Get top skills for a specific company", description = "Shows the most important skills by job count for a company")
    public ResponseEntity<List<CompanyTopSkillsResponseDto>> getCompanyTopSkills(
            @RequestParam UUID companyId,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(analyticsService.getCompanyTopSkills(companyId, limit));
    }

    @GetMapping("/company-role-distribution")
    @Operation(summary = "Get role distribution for a specific company", description = "Shows how roles are distributed for a company")
    public ResponseEntity<List<CompanyRoleDistributionResponseDto>> getCompanyRoleDistribution(
            @RequestParam UUID companyId
    ) {
        return ResponseEntity.ok(analyticsService.getCompanyRoleDistribution(companyId));
    }

    @GetMapping("/company-work-mode-distribution")
    @Operation(summary = "Get work mode distribution for a specific company", description = "Shows remote/hybrid/onsite distribution for a company")
    public ResponseEntity<List<CompanyWorkModeDistributionResponseDto>> getCompanyWorkModeDistribution(
            @RequestParam UUID companyId
    ) {
        return ResponseEntity.ok(analyticsService.getCompanyWorkModeDistribution(companyId));
    }

    @GetMapping("/compare-companies")
    @Operation(
            summary = "Compare two companies side-by-side",
            description = "Returns a dashboard-friendly comparison of two companies including top skills, role distribution, and work mode distribution"
    )
    public ResponseEntity<CompanyComparisonResponseDto> compareCompanies(
            @RequestParam UUID companyA,
            @RequestParam UUID companyB
    ) {
        return ResponseEntity.ok(analyticsService.compareCompanies(companyA, companyB));
    }
}

