package com.emreuslu.techstack.backend.job.service;

import com.emreuslu.techstack.backend.common.exception.ResourceNotFoundException;
import com.emreuslu.techstack.backend.company.entity.Company;
import com.emreuslu.techstack.backend.company.repository.CompanyRepository;
import com.emreuslu.techstack.backend.job.dto.CreateJobRequestDto;
import com.emreuslu.techstack.backend.job.dto.JobResponseDto;
import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

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
}

