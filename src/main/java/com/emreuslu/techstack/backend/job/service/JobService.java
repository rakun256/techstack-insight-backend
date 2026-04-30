package com.emreuslu.techstack.backend.job.service;

import com.emreuslu.techstack.backend.common.exception.ResourceNotFoundException;
import com.emreuslu.techstack.backend.company.entity.Company;
import com.emreuslu.techstack.backend.company.repository.CompanyRepository;
import com.emreuslu.techstack.backend.job.dto.CreateJobRequestDto;
import com.emreuslu.techstack.backend.job.dto.JobFilterCriteria;
import com.emreuslu.techstack.backend.job.dto.JobResponseDto;
import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import com.emreuslu.techstack.backend.jobskill.repository.JobSkillRepository;
import com.emreuslu.techstack.backend.skill.repository.SkillRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final JobSkillRepository jobSkillRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public JobResponseDto createJob(CreateJobRequestDto request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + request.companyId()));

        Job job = Job.builder()
                .externalId(request.externalId())
                .source(request.source())
                .title(request.title())
                .normalizedTitle(request.title())
                .softwareRelevant(false)
                .roleFamily(null)
                .roleSubfamily(null)
                .location(request.location())
                .locationNormalized(request.location())
                .country(null)
                .remote(false)
                .hybrid(false)
                .description(request.description())
                .applyUrl(request.applyUrl())
                .postedAt(request.postedAt())
                .company(company)
                .build();

        Job savedJob = jobRepository.save(job);
        return toResponseDto(savedJob);
    }

    @Transactional(readOnly = true)
    public List<JobResponseDto> getAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobResponseDto getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        return toResponseDto(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponseDto> searchJobs(JobFilterCriteria criteria) {
        return searchJobsRaw(criteria).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    protected List<Job> searchJobsRaw(JobFilterCriteria criteria) {
        List<Job> jobs;

        // If skill filtering is requested, handle it separately
        if (criteria.skillId() != null || criteria.skillName() != null) {
            jobs = filterJobsBySkill(criteria);
        } else {
            // Use repository filtering for other criteria
            jobs = jobRepository.findByFilters(
                    criteria.roleFamily(),
                    criteria.country(),
                    criteria.remote(),
                    criteria.hybrid(),
                    criteria.companyId(),
                    criteria.source(),
                    criteria.postedAtFrom(),
                    criteria.postedAtTo(),
                    criteria.titleQuery()
            );
        }

        // If we have additional criteria beyond skill, apply them post-query
        if (criteria.skillId() == null && criteria.skillName() == null) {
            return jobs;
        }

        // For skill-filtered results, apply other criteria as well
        return jobs.stream()
                .filter(j -> criteria.roleFamily() == null || j.getRoleFamily().equals(criteria.roleFamily()))
                .filter(j -> criteria.country() == null || j.getCountry().equals(criteria.country()))
                .filter(j -> criteria.remote() == null || j.isRemote() == criteria.remote())
                .filter(j -> criteria.hybrid() == null || j.isHybrid() == criteria.hybrid())
                .filter(j -> criteria.companyId() == null || j.getCompany().getId().equals(criteria.companyId()))
                .filter(j -> criteria.source() == null || j.getSource().equals(criteria.source()))
                .filter(j -> criteria.postedAtFrom() == null || !j.getPostedAt().isBefore(criteria.postedAtFrom()))
                .filter(j -> criteria.postedAtTo() == null || !j.getPostedAt().isAfter(criteria.postedAtTo()))
                .filter(j -> criteria.titleQuery() == null || j.getNormalizedTitle().toLowerCase()
                        .contains(criteria.titleQuery().toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<Job> filterJobsBySkill(JobFilterCriteria criteria) {
        List<Job> skillFilteredJobs;

        if (criteria.skillId() != null) {
            skillFilteredJobs = jobSkillRepository.findJobsBySkillId(criteria.skillId());
        } else {
            skillFilteredJobs = jobSkillRepository.findJobsBySkillName(criteria.skillName());
        }

        return skillFilteredJobs;
    }

    private JobResponseDto toResponseDto(Job job) {
        return new JobResponseDto(
                job.getId(),
                job.getExternalId(),
                job.getSource(),
                job.getTitle(),
                job.getNormalizedTitle(),
                job.isSoftwareRelevant(),
                job.getRoleFamily(),
                job.getRoleSubfamily(),
                job.getLocation(),
                job.getLocationNormalized(),
                job.getCountry(),
                job.isRemote(),
                job.isHybrid(),
                job.getDescription(),
                job.getApplyUrl(),
                job.getPostedAt(),
                job.getCompany().getId(),
                job.getCompany().getName()
        );
    }

    @Transactional(readOnly = true)
    public Page<JobResponseDto> getAllJobsPaged(Pageable pageable) {
        Page<Job> jobs = jobRepository.findAll(pageable);
        List<JobResponseDto> content = jobs.getContent()
                .stream()
                .map(this::toResponseDto)
                .toList();
        return new PageImpl<>(content, pageable, jobs.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<JobResponseDto> searchJobsPaged(JobFilterCriteria criteria, Pageable pageable) {
        List<Job> jobs = searchJobsRaw(criteria);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), jobs.size());
        List<JobResponseDto> content = jobs.subList(start, end)
                .stream()
                .map(this::toResponseDto)
                .toList();
        return new PageImpl<>(content, pageable, jobs.size());
    }
}

