package com.emreuslu.techstack.backend.ingestion.service;

import com.emreuslu.techstack.backend.ingestion.dto.RoleClassificationResultDto;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SoftwareRoleClassificationService {

    private static final List<String> NON_TECH_KEYWORDS = List.of(
            "sales", "account executive", "account development", "business development", "customer success",
            "marketing", "finance", "recruiting", "talent", "legal", "operations"
    );

    private static final List<String> TECH_SIGNALS = List.of(
            "java", "python", "typescript", "javascript", "react", "spring", "kubernetes", "docker",
            "aws", "gcp", "azure", "ci/cd", "api", "backend", "frontend", "machine learning", "ml"
    );

    public RoleClassificationResultDto classify(
            String rawTitle,
            String departmentRaw,
            String teamRaw,
            String analysisText,
            TextNormalizationService textNormalizationService
    ) {
        String normalizedTitle = textNormalizationService.normalizeTitle(rawTitle);
        String titleLower = lower(normalizedTitle);
        String departmentLower = lower(departmentRaw);
        String teamLower = lower(teamRaw);
        String analysisLower = lower(analysisText);

        if (containsAny(titleLower, NON_TECH_KEYWORDS)
                || containsAny(departmentLower, NON_TECH_KEYWORDS)
                || containsAny(teamLower, NON_TECH_KEYWORDS)) {
            return new RoleClassificationResultDto(
                    normalizedTitle,
                    classifyNonTechFamily(titleLower, departmentLower, teamLower),
                    "NON_SOFTWARE",
                    false,
                    5,
                    "non-tech keyword match"
            );
        }

        String family = classifyTechFamily(titleLower, analysisLower);
        int score = calculateRelevanceScore(titleLower, analysisLower);
        boolean relevant = family != null || score >= 40;

        if (!relevant) {
            return new RoleClassificationResultDto(
                    normalizedTitle,
                    "OTHER_NON_TECH",
                    "UNKNOWN",
                    false,
                    score,
                    "insufficient software signals"
            );
        }

        String resolvedFamily = family != null ? family : "SOFTWARE_ENGINEERING_GENERAL";
        return new RoleClassificationResultDto(
                normalizedTitle,
                resolvedFamily,
                "GENERAL",
                true,
                Math.max(score, 50),
                "software role keywords and technical signals"
        );
    }

    private String classifyTechFamily(String titleLower, String analysisLower) {
        if (containsAny(titleLower, List.of("developer experience", "devex"))) {
            return "DEVELOPER_EXPERIENCE";
        }
        if (containsAny(titleLower, List.of("machine learning", "ml engineer", "ai ", " ai"))) {
            return "MACHINE_LEARNING_AI";
        }
        if (containsAny(titleLower, List.of("backend", "back-end", "java backend", "python backend"))) {
            return "BACKEND";
        }
        if (containsAny(titleLower, List.of("frontend", "front-end", "ui engineer"))) {
            return "FRONTEND";
        }
        if (containsAny(titleLower, List.of("fullstack", "full stack"))) {
            return "FULLSTACK";
        }
        if (containsAny(titleLower, List.of("devops", "platform", "sre", "site reliability"))) {
            return "DEVOPS_PLATFORM";
        }
        if (containsAny(titleLower, List.of("infrastructure", "deployment", "sdk"))) {
            return "INFRASTRUCTURE";
        }
        if (containsAny(titleLower, List.of("data engineer", "analytics engineer"))) {
            return "DATA";
        }
        if (containsAny(titleLower, List.of("security", "application security"))) {
            return "SECURITY";
        }
        if (containsAny(titleLower, List.of("android", "ios", "mobile"))) {
            return "MOBILE";
        }
        if (containsAny(titleLower, List.of("qa", "test engineer", "sdet"))) {
            return "QA_TEST";
        }
        if (containsAny(titleLower, List.of("software engineer", "software developer", "engineer"))
                && containsAny(analysisLower, TECH_SIGNALS)) {
            return "SOFTWARE_ENGINEERING_GENERAL";
        }
        return null;
    }

    private String classifyNonTechFamily(String titleLower, String departmentLower, String teamLower) {
        String combined = String.join(" ", nullToEmpty(titleLower), nullToEmpty(departmentLower), nullToEmpty(teamLower));
        if (combined.contains("sales") || combined.contains("account executive") || combined.contains("business development")) {
            return "SALES";
        }
        if (combined.contains("customer success")) {
            return "CUSTOMER_SUCCESS";
        }
        if (combined.contains("marketing")) {
            return "MARKETING";
        }
        if (combined.contains("finance")) {
            return "FINANCE";
        }
        if (combined.contains("recruit") || combined.contains("talent")) {
            return "RECRUITING";
        }
        if (combined.contains("operations")) {
            return "OPERATIONS";
        }
        if (combined.contains("legal")) {
            return "LEGAL";
        }
        return "OTHER_NON_TECH";
    }

    private int calculateRelevanceScore(String titleLower, String analysisLower) {
        int score = 0;

        if (containsAny(titleLower, List.of("engineer", "developer", "software", "platform", "devops", "data", "ml", "ai"))) {
            score += 35;
        }

        for (String signal : TECH_SIGNALS) {
            if (contains(signal, analysisLower)) {
                score += 8;
            }
        }

        return Math.min(score, 100);
    }

    private boolean containsAny(String text, List<String> keywords) {
        if (text == null || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (contains(keyword, text)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(String keyword, String text) {
        return keyword != null && text != null && text.contains(keyword.toLowerCase(Locale.ROOT));
    }

    private String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

