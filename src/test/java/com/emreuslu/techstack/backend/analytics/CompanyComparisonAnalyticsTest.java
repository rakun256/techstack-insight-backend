package com.emreuslu.techstack.backend.analytics;

import com.emreuslu.techstack.backend.analytics.dto.CompanyRoleDistributionResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.CompanyTopSkillsResponseDto;
import com.emreuslu.techstack.backend.analytics.dto.CompanyWorkModeDistributionResponseDto;
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
@DisplayName("Company Comparison Analytics Tests")
@Transactional
class CompanyComparisonAnalyticsTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSkillRepository jobSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    private Company testCompany;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        testCompany = companyRepository.save(Company.builder()
                .name("Test Company")
                .externalSource("GREENHOUSE")
                .externalCompanyId("ext-" + System.nanoTime())
                .build());
        companyId = testCompany.getId();
    }

    private Job createJobForCompany(String title, String roleFamily, boolean remote, boolean hybrid) {
        return jobRepository.save(Job.builder()
                .externalId("job-" + System.nanoTime())
                .source("GREENHOUSE")
                .title(title)
                .normalizedTitle(title.toLowerCase())
                .softwareRelevant(true)
                .roleFamily(roleFamily)
                .roleSubfamily("JUNIOR")
                .location("Berlin")
                .locationNormalized("Berlin")
                .country("Germany")
                .remote(remote)
                .hybrid(hybrid)
                .description("Test job")
                .applyUrl("https://apply.example.com")
                .postedAt(LocalDate.now())
                .company(testCompany)
                .build());
    }

    @Test
    @DisplayName("Should return top skills for a company")
    void testGetCompanyTopSkills() {
        Skill pythonSkill = skillRepository.save(Skill.builder()
                .name("Python")
                .build());
        Skill javaSkill = skillRepository.save(Skill.builder()
                .name("Java")
                .build());

        Job job1 = createJobForCompany("Backend Engineer", "BACKEND", false, true);
        Job job2 = createJobForCompany("Backend Lead", "BACKEND", false, true);

        jobSkillRepository.save(JobSkill.builder().job(job1).skill(pythonSkill).build());
        jobSkillRepository.save(JobSkill.builder().job(job2).skill(javaSkill).build());

        List<CompanyTopSkillsResponseDto> result = analyticsService.getCompanyTopSkills(companyId, null);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(dto -> companyId.equals(dto.companyId()));
        assertThat(result).anyMatch(dto -> "Python".equals(dto.skillName()) || "Java".equals(dto.skillName()));
    }

    @Test
    @DisplayName("Should return role distribution for a company")
    void testGetCompanyRoleDistribution() {
        Job backendJob = createJobForCompany("Backend Engineer", "BACKEND", false, true);
        Job frontendJob = createJobForCompany("Frontend Developer", "FRONTEND", false, true);

        List<CompanyRoleDistributionResponseDto> result = analyticsService.getCompanyRoleDistribution(companyId);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(dto -> companyId.equals(dto.companyId()));
    }

    @Test
    @DisplayName("Should return work mode distribution for a company")
    void testGetCompanyWorkModeDistribution() {
        Job remoteJob = createJobForCompany("Remote Developer", "BACKEND", true, false);
        Job hybridJob = createJobForCompany("Hybrid Developer", "BACKEND", false, true);

        List<CompanyWorkModeDistributionResponseDto> result = analyticsService.getCompanyWorkModeDistribution(companyId);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(dto -> companyId.equals(dto.companyId()));
    }

    @Test
    @DisplayName("Should limit top skills results when limit parameter provided")
    void testCompanyTopSkillsWithLimit() {
        Skill skill1 = skillRepository.save(Skill.builder().name("Python").build());
        Skill skill2 = skillRepository.save(Skill.builder().name("Java").build());
        Skill skill3 = skillRepository.save(Skill.builder().name("JavaScript").build());

        Job job1 = createJobForCompany("Backend Engineer 1", "BACKEND", false, true);
        Job job2 = createJobForCompany("Backend Engineer 2", "BACKEND", false, true);
        Job job3 = createJobForCompany("Backend Engineer 3", "BACKEND", false, true);

        jobSkillRepository.save(JobSkill.builder().job(job1).skill(skill1).build());
        jobSkillRepository.save(JobSkill.builder().job(job2).skill(skill2).build());
        jobSkillRepository.save(JobSkill.builder().job(job3).skill(skill3).build());

        List<CompanyTopSkillsResponseDto> result = analyticsService.getCompanyTopSkills(companyId, 2);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should include company name in results")
    void testCompanyNameIncluded() {
        Skill skill = skillRepository.save(Skill.builder().name("Python").build());
        Job job = createJobForCompany("Developer", "BACKEND", false, true);
        jobSkillRepository.save(JobSkill.builder().job(job).skill(skill).build());

        List<CompanyTopSkillsResponseDto> result = analyticsService.getCompanyTopSkills(companyId, null);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(dto -> "Test Company".equals(dto.companyName()));
    }

    @Test
    @DisplayName("Should handle company with no jobs")
    void testCompanyWithNoJobs() {
        UUID emptiedCompanyId = UUID.randomUUID();

        List<CompanyTopSkillsResponseDto> result = analyticsService.getCompanyTopSkills(emptiedCompanyId, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should correctly count jobs per role family")
    void testRoleDistributionCounting() {
        // Create 2 backend jobs and 1 frontend job
        createJobForCompany("Backend 1", "BACKEND", false, true);
        createJobForCompany("Backend 2", "BACKEND", false, true);
        createJobForCompany("Frontend 1", "FRONTEND", false, true);

        List<CompanyRoleDistributionResponseDto> result = analyticsService.getCompanyRoleDistribution(companyId);

        assertThat(result).isNotEmpty();
        assertThat(result)
                .filteredOn(dto -> "BACKEND".equals(dto.roleFamily()))
                .allMatch(dto -> dto.jobCount() >= 2);
    }
}

