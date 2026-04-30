package com.emreuslu.techstack.backend.job.controller;

import com.emreuslu.techstack.backend.job.dto.CreateJobRequestDto;
import com.emreuslu.techstack.backend.job.dto.JobFilterCriteria;
import com.emreuslu.techstack.backend.job.dto.JobResponseDto;
import com.emreuslu.techstack.backend.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job CRUD read/write operations")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @Operation(summary = "Create a new job")
    public ResponseEntity<JobResponseDto> createJob(@Validated @RequestBody CreateJobRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request));
    }

    @GetMapping
    @Operation(
            summary = "List all jobs with optional filtering and pagination",
            description = "Returns all jobs or filtered results based on criteria. " +
                    "Supports optional pagination with page and size parameters. " +
                    "When no filters are provided, returns all jobs (paged). " +
                    "All filter parameters are optional and can be combined."
    )
    public ResponseEntity<Page<JobResponseDto>> getAllJobs(
            @Parameter(description = "Filter by role family (e.g., BACKEND, FRONTEND, DEVOPS)")
            @RequestParam(required = false) String roleFamily,

            @Parameter(description = "Filter by country")
            @RequestParam(required = false) String country,

            @Parameter(description = "Filter by remote jobs")
            @RequestParam(required = false) Boolean remote,

            @Parameter(description = "Filter by hybrid jobs")
            @RequestParam(required = false) Boolean hybrid,

            @Parameter(description = "Filter by company ID")
            @RequestParam(required = false) UUID companyId,

            @Parameter(description = "Filter by skill ID")
            @RequestParam(required = false) Long skillId,

            @Parameter(description = "Filter by skill name")
            @RequestParam(required = false) String skillName,

            @Parameter(description = "Filter by source (e.g., GREENHOUSE, LEVER)")
            @RequestParam(required = false) String source,

            @Parameter(description = "Filter by posted date from (inclusive)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate postedAtFrom,

            @Parameter(description = "Filter by posted date to (inclusive)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate postedAtTo,

            @Parameter(description = "Filter by job title (case-insensitive, partial match)")
            @RequestParam(required = false) String titleQuery,

            @Parameter(description = "Page number (0-indexed), default 0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size, default 20")
            @RequestParam(defaultValue = "20") int size
    ) {
        JobFilterCriteria criteria = new JobFilterCriteria(
                roleFamily, country, remote, hybrid, companyId, skillId, skillName,
                source, postedAtFrom, postedAtTo, titleQuery
        );

        Pageable pageable = PageRequest.of(page, size);

        if (!criteria.hasAnyFilter()) {
            return ResponseEntity.ok(jobService.getAllJobsPaged(pageable));
        }

        return ResponseEntity.ok(jobService.searchJobsPaged(criteria, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by id")
    public ResponseEntity<JobResponseDto> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }
}

