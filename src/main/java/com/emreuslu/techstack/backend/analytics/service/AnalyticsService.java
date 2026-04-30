package com.emreuslu.techstack.backend.analytics.service;


import com.emreuslu.techstack.backend.analytics.dto.CompanyComparisonResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.CompanyComparisonResponseDto.CompanySnapshot;
import com.emreuslu.techstack.backend.analytics.dto.CompanyComparisonResponseDto.CompanySnapshot.RoleEntry;
import com.emreuslu.techstack.backend.analytics.dto.CompanyComparisonResponseDto.CompanySnapshot.SkillEntry;
import com.emreuslu.techstack.backend.analytics.dto.CompanyComparisonResponseDto.CompanySnapshot.WorkModeEntry;
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
import com.emreuslu.techstack.backend.common.exception.ResourceNotFoundException;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import com.emreuslu.techstack.backend.jobskill.repository.JobSkillRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final JobSkillRepository jobSkillRepository;
    private final JobRepository jobRepository;

    @Transactional(readOnly = true)
    public List<TopSkillResponseDto> getTopSkills(Integer limit) {
        List<TopSkillResponseDto> result = jobSkillRepository.findTopSkillCounts()
                .stream()
                .map(projection -> new TopSkillResponseDto(
                        projection.getSkillName(),
                        projection.getJobCount()
                ))
                .toList();

        if (limit == null || limit <= 0 || limit >= result.size()) {
            return result;
        }

        return result.subList(0, limit);
    }

    @Transactional(readOnly = true)
    public List<LocationTrendResponseDto> getLocationTrends() {
        return jobRepository.findLocationTrends()
                .stream()
                .map(projection -> new LocationTrendResponseDto(
                        projection.getLocation(),
                        projection.getJobCount()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleSkillDistributionResponseDto> getRoleSkillDistribution() {
        return jobSkillRepository.findRoleSkillDistribution()
                .stream()
                .map(projection -> new RoleSkillDistributionResponseDto(
                        projection.getJobTitle(),
                        projection.getSkillName(),
                        projection.getJobCount()
                ))
                .toList();
    }

    // Time-series analytics

    @Transactional(readOnly = true)
    public List<TrendingSkillResponseDto> getTrendingSkills(Integer days) {
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(days != null ? days : 30);

        return jobSkillRepository.findTopSkillCounts()  // Using all-time for reference
                .stream()
                .limit(20)  // Top 20 skills by default
                .toList()
                .stream()
                .map(projection -> new TrendingSkillResponseDto(
                        toDate,
                        projection.getSkillName(),
                        projection.getJobCount()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleFamilyTrendResponseDto> getRoleFamilyTrends(Integer days) {
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(days != null ? days : 30);

        return jobRepository.findRoleFamilyTrends(fromDate, toDate)
                .stream()
                .map(projection -> new RoleFamilyTrendResponseDto(
                        projection.getDateBucket(),
                        projection.getRoleFamily(),
                        projection.getJobCount()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CountrySkillTrendResponseDto> getCountrySkillTrends(String country, Integer days) {
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country parameter is required");
        }

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(days != null ? days : 30);

        return jobRepository.findCountrySkillTrends(country, fromDate, toDate)
                .stream()
                .map(projection -> new CountrySkillTrendResponseDto(
                        projection.getDateBucket(),
                        projection.getSkillName(),
                        projection.getJobCount()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkModeTrendResponseDto> getWorkModeTrends(Integer days) {
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(days != null ? days : 30);

        return jobRepository.findWorkModeTrends(fromDate, toDate)
                .stream()
                .map(projection -> new WorkModeTrendResponseDto(
                        projection.getDateBucket(),
                        projection.getWorkMode(),
                        projection.getJobCount()
                ))
                .toList();
    }

    // Company analytics

    @Transactional(readOnly = true)
    public List<CompanyTopSkillsResponseDto> getCompanyTopSkills(UUID companyId, Integer limit) {
        List<CompanyTopSkillsResponseDto> result = jobSkillRepository.findTopSkillsByCompany(companyId)
                .stream()
                .map(projection -> new CompanyTopSkillsResponseDto(
                        projection.getCompanyId(),
                        projection.getCompanyName(),
                        projection.getSkillName(),
                        projection.getJobCount()
                ))
                .toList();

        if (limit == null || limit <= 0 || limit >= result.size()) {
            return result;
        }

        return result.subList(0, limit);
    }

    @Transactional(readOnly = true)
    public List<CompanyRoleDistributionResponseDto> getCompanyRoleDistribution(UUID companyId) {
        return jobSkillRepository.findRoleDistributionByCompany(companyId)
                .stream()
                .map(projection -> new CompanyRoleDistributionResponseDto(
                        projection.getCompanyId(),
                        projection.getCompanyName(),
                        projection.getRoleFamily(),
                        projection.getJobCount()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyWorkModeDistributionResponseDto> getCompanyWorkModeDistribution(UUID companyId) {
        return jobSkillRepository.findWorkModeDistributionByCompany(companyId)
                .stream()
                .map(projection -> new CompanyWorkModeDistributionResponseDto(
                        projection.getCompanyId(),
                        projection.getCompanyName(),
                        projection.getWorkMode(),
                        projection.getJobCount()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyComparisonResponseDto compareCompanies(UUID companyAId, UUID companyBId) {
        CompanySnapshot snapshotA = buildCompanySnapshot(companyAId);
        CompanySnapshot snapshotB = buildCompanySnapshot(companyBId);
        return new CompanyComparisonResponseDto(snapshotA, snapshotB);
    }

    private CompanySnapshot buildCompanySnapshot(UUID companyId) {
        List<CompanyTopSkillsResponseDto> topSkills = getCompanyTopSkills(companyId, 10);
        List<CompanyRoleDistributionResponseDto> roleDistribution = getCompanyRoleDistribution(companyId);
        List<CompanyWorkModeDistributionResponseDto> workModeDistribution = getCompanyWorkModeDistribution(companyId);

        String companyName = topSkills.isEmpty() && roleDistribution.isEmpty() && workModeDistribution.isEmpty()
                ? "Unknown Company"
                : topSkills.isEmpty() ? roleDistribution.get(0).companyName() : topSkills.get(0).companyName();

        return new CompanySnapshot(
                companyId,
                companyName,
                topSkills.stream()
                        .map(dto -> new SkillEntry(dto.skillName(), dto.jobCount()))
                        .toList(),
                roleDistribution.stream()
                        .map(dto -> new RoleEntry(dto.roleFamily(), dto.jobCount()))
                        .toList(),
                workModeDistribution.stream()
                        .map(dto -> new WorkModeEntry(dto.workMode(), dto.jobCount()))
                        .toList()
        );
    }
}

