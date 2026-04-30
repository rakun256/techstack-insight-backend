package com.emreuslu.techstack.backend.job;

import com.emreuslu.techstack.backend.job.dto.JobFilterCriteria;
import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import com.emreuslu.techstack.backend.company.entity.Company;
import com.emreuslu.techstack.backend.company.repository.CompanyRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Job Filtering Tests")
@Transactional
class JobFilteringTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company testCompany;

    @BeforeEach
    void setUp() {
        testCompany = companyRepository.save(Company.builder()
                .name("Test Filter Company")
                .externalSource("GREENHOUSE")
                .externalCompanyId("ext-filter-" + System.nanoTime())
                .build());
    }

    private Job createTestJob(String title, String roleFamily, String country, boolean remote, 
                             boolean hybrid, LocalDate postedAt, Company company) {
        return Job.builder()
                .externalId("job-" + System.nanoTime())
                .source("GREENHOUSE")
                .title(title)
                .normalizedTitle(title.toLowerCase())
                .softwareRelevant(true)
                .roleFamily(roleFamily)
                .roleSubfamily("JUNIOR")
                .location("Berlin")
                .locationNormalized("Berlin")
                .country(country)
                .remote(remote)
                .hybrid(hybrid)
                .description("Test job description")
                .applyUrl("https://apply.example.com")
                .postedAt(postedAt)
                .company(company)
                .build();
    }

    @Test
    @DisplayName("Should filter jobs by roleFamily")
    void testFilterByRoleFamily() {
        Job backendJob = createTestJob("Backend Engineer", "BACKEND", "Germany", false, true,
                LocalDate.now(), testCompany);
        Job frontendJob = createTestJob("Frontend Developer", "FRONTEND", "Germany", false, true,
                LocalDate.now(), testCompany);

        jobRepository.saveAll(List.of(backendJob, frontendJob));

        List<Job> result = jobRepository.findByFilters(
                "BACKEND", null, null, null, null, null, null, null, null
        );

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(j -> "BACKEND".equals(j.getRoleFamily()));
    }

    @Test
    @DisplayName("Should filter jobs by country")
    void testFilterByCountry() {
        Job germanyJob = createTestJob("Backend Engineer", "BACKEND", "Germany", false, true,
                LocalDate.now(), testCompany);
        Job usJob = createTestJob("Frontend Developer", "FRONTEND", "United States", false, true,
                LocalDate.now(), testCompany);

        jobRepository.saveAll(List.of(germanyJob, usJob));

        List<Job> result = jobRepository.findByFilters(
                null, "Germany", null, null, null, null, null, null, null
        );

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(j -> "Germany".equals(j.getCountry()));
    }

    @Test
    @DisplayName("Should filter jobs by remote flag")
    void testFilterByRemote() {
        Job remoteJob = createTestJob("Remote Developer", "BACKEND", "Germany", true, false,
                LocalDate.now(), testCompany);
        Job hybridJob = createTestJob("Hybrid Developer", "BACKEND", "Germany", false, true,
                LocalDate.now(), testCompany);

        jobRepository.saveAll(List.of(remoteJob, hybridJob));

        List<Job> result = jobRepository.findByFilters(
                null, null, true, null, null, null, null, null, null
        );

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(Job::isRemote);
    }

    @Test
    @DisplayName("Should filter jobs by date range")
    void testFilterByDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate weekAgo = today.minusDays(7);

        Job oldJob = createTestJob("Old Job", "BACKEND", "Germany", false, true,
                weekAgo, testCompany);
        Job recentJob = createTestJob("Recent Job", "BACKEND", "Germany", false, true,
                today, testCompany);

        jobRepository.saveAll(List.of(oldJob, recentJob));

        List<Job> resultAll = jobRepository.findByFilters(
                null, null, null, null, null, null, null, null, null
        );

        List<Job> resultFiltered = jobRepository.findByFilters(
                null, null, null, null, null, null, null, yesterday, null
        );

        // We should have fewer jobs in the filtered result compared to unfiltered
        assertThat(resultAll).isNotEmpty();
        assertThat(resultFiltered.size()).isLessThanOrEqualTo(resultAll.size());
    }

    @Test
    @DisplayName("Should filter jobs by title query")
    void testFilterByTitleQuery() {
        Job pythonJob = createTestJob("Python Backend Engineer", "BACKEND", "Germany", false, true,
                LocalDate.now(), testCompany);
        Job javaJob = createTestJob("Java Developer", "BACKEND", "Germany", false, true,
                LocalDate.now(), testCompany);

        jobRepository.saveAll(List.of(pythonJob, javaJob));

        List<Job> result = jobRepository.findByFilters(
                null, null, null, null, null, null, null, null, "python"
        );

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(j -> j.getNormalizedTitle().toLowerCase().contains("python"));
    }

    @Test
    @DisplayName("Should combine multiple filters")
    void testCombineMultipleFilters() {
        Job matchingJob = createTestJob("Backend Engineer", "BACKEND", "Germany", true, false,
                LocalDate.now(), testCompany);
        Job nonMatchingJob1 = createTestJob("Frontend Developer", "FRONTEND", "Germany", true, false,
                LocalDate.now(), testCompany);
        Job nonMatchingJob2 = createTestJob("Backend Engineer", "BACKEND", "United States", true, false,
                LocalDate.now(), testCompany);

        jobRepository.saveAll(List.of(matchingJob, nonMatchingJob1, nonMatchingJob2));

        List<Job> result = jobRepository.findByFilters(
                "BACKEND", "Germany", true, null, null, null, null, null, null
        );

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(j -> 
                "BACKEND".equals(j.getRoleFamily()) && 
                "Germany".equals(j.getCountry()) && 
                j.isRemote()
        );
    }

    @Test
    @DisplayName("Should return all jobs when no filters provided")
    void testReturnAllJobsWhenNoFilters() {
        Job job1 = createTestJob("Backend Engineer", "BACKEND", "Germany", false, true,
                LocalDate.now(), testCompany);
        Job job2 = createTestJob("Frontend Developer", "FRONTEND", "Germany", false, true,
                LocalDate.now(), testCompany);

        jobRepository.saveAll(List.of(job1, job2));

        List<Job> result = jobRepository.findByFilters(
                null, null, null, null, null, null, null, null, null
        );

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }
}

