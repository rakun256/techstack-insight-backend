package com.emreuslu.techstack.backend.analytics.service;

import com.emreuslu.techstack.backend.analytics.dto.LocationTrendResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.RoleSkillDistributionResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.TopSkillResponseDto;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import com.emreuslu.techstack.backend.jobskill.repository.JobSkillRepository;
import java.util.List;
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
}

