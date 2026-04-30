package com.emreuslu.techstack.backend.analytics;

import com.emreuslu.techstack.backend.analytics.dto.TrendingSkillResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.RoleFamilyTrendResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.WorkModeTrendResponseDto;
import com.emreuslu.techstack.backend.analytics.service.AnalyticsService;
import com.emreuslu.techstack.backend.job.entity.Job;
import com.emreuslu.techstack.backend.job.repository.JobRepository;
import com.emreuslu.techstack.backend.jobskill.entity.JobSkill;
import com.emreuslu.techstack.backend.jobskill.repository.JobSkillRepository;
import com.emreuslu.techstack.backend.skill.entity.Skill;
import com.emreuslu.techstack.backend.skill.repository.SkillRepository;
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
@DisplayName("Trending Skills Analytics Tests")
@Transactional
class TrendingSkillsAnalyticsTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSkillRepository jobSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company testCompany;

    @BeforeEach
    void setUp() {
        testCompany = companyRepository.save(Company.builder()
                .name("Test Company")
                .externalSource("GREENHOUSE")
                .externalCompanyId("ext-" + System.nanoTime())
                .build());
    }

    private Job createTestJob(LocalDate postedAt, String roleFamily) {
        return jobRepository.save(Job.builder()
                .externalId("job-" + System.nanoTime())
                .source("GREENHOUSE")
                .title("Backend Engineer")
                .normalizedTitle("backend engineer")
                .softwareRelevant(true)
                .roleFamily(roleFamily)
                .roleSubfamily("JUNIOR")
                .location("Berlin")
                .locationNormalized("Berlin")
                .country("Germany")
                .remote(true)
                .hybrid(false)
                .description("Test job")
                .applyUrl("https://apply.example.com")
                .postedAt(postedAt)
                .company(testCompany)
                .build());
    }

    @Test
    @DisplayName("Should return trending skills for recent period")
    void testGetTrendingSkills() {
        Skill pythonSkill = skillRepository.save(Skill.builder()
                .name("Python")
                .build());

        Job job = createTestJob(LocalDate.now(), "BACKEND");
        jobSkillRepository.save(JobSkill.builder()
                .job(job)
                .skill(pythonSkill)
                .build());

        List<TrendingSkillResponseDto> result = analyticsService.getTrendingSkills(30);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Should return role family trends")
    void testGetRoleFamilyTrends() {
        Job backendJob = createTestJob(LocalDate.now(), "BACKEND");
        Job frontendJob = createTestJob(LocalDate.now().minusDays(1), "FRONTEND");

        jobRepository.saveAll(List.of(backendJob, frontendJob));

        List<RoleFamilyTrendResponseDto> result = analyticsService.getRoleFamilyTrends(30);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Should return work mode trends")
    void testGetWorkModeTrends() {
        Job remoteJob = jobRepository.save(Job.builder()
                .externalId("job-remote")
                .source("GREENHOUSE")
                .title("Remote Developer")
                .normalizedTitle("remote developer")
                .softwareRelevant(true)
                .roleFamily("BACKEND")
                .roleSubfamily("JUNIOR")
                .location("Remote")
                .locationNormalized("Remote")
                .country("Germany")
                .remote(true)
                .hybrid(false)
                .description("Test job")
                .applyUrl("https://apply.example.com")
                .postedAt(LocalDate.now())
                .company(testCompany)
                .build());

        Job hybridJob = jobRepository.save(Job.builder()
                .externalId("job-hybrid")
                .source("GREENHOUSE")
                .title("Hybrid Developer")
                .normalizedTitle("hybrid developer")
                .softwareRelevant(true)
                .roleFamily("BACKEND")
                .roleSubfamily("JUNIOR")
                .location("Berlin")
                .locationNormalized("Berlin")
                .country("Germany")
                .remote(false)
                .hybrid(true)
                .description("Test job")
                .applyUrl("https://apply.example.com")
                .postedAt(LocalDate.now().minusDays(1))
                .company(testCompany)
                .build());

        List<WorkModeTrendResponseDto> result = analyticsService.getWorkModeTrends(30);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Should filter by number of days")
    void testTrendingSkillsFilterByDays() {
        Skill skill = skillRepository.save(Skill.builder()
                .name("Python")
                .build());

        Job recentJob = createTestJob(LocalDate.now(), "BACKEND");
        jobSkillRepository.save(JobSkill.builder()
                .job(recentJob)
                .skill(skill)
                .build());

        List<TrendingSkillResponseDto> result7Days = analyticsService.getTrendingSkills(7);
        List<TrendingSkillResponseDto> result30Days = analyticsService.getTrendingSkills(30);

        assertThat(result30Days.size()).isGreaterThanOrEqualTo(result7Days.size());
    }

    @Test
    @DisplayName("Should handle empty result gracefully")
    void testEmptyTrendingResults() {
        List<TrendingSkillResponseDto> result = analyticsService.getTrendingSkills(1);

        // Should return empty list or handle gracefully, not throw exception
        assertThat(result).isNotNull();
    }
}

