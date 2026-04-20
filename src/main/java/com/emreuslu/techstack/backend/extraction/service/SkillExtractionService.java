package com.emreuslu.techstack.backend.extraction.service;

import com.emreuslu.techstack.backend.extraction.catalog.SkillKeywordCatalog;
import com.emreuslu.techstack.backend.extraction.dto.ExtractedSkillDto;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkillExtractionService {

    private final SkillKeywordCatalog skillKeywordCatalog;

    public List<ExtractedSkillDto> extractSkills(String text) {
        String normalizedText = normalizeText(text);
        if (normalizedText == null) {
            return List.of();
        }

        Map<String, ExtractedSkillDto> uniqueSkills = new LinkedHashMap<>();

        for (SkillKeywordCatalog.SkillKeywordEntry entry : skillKeywordCatalog.entries()) {
            for (String keyword : entry.keywords()) {
                if (matchesKeyword(normalizedText, keyword)) {
                    uniqueSkills.putIfAbsent(
                            entry.canonicalSkillName(),
                            new ExtractedSkillDto(entry.canonicalSkillName(), keyword)
                    );
                    break;
                }
            }
        }

        return new ArrayList<>(uniqueSkills.values());
    }

    private boolean matchesKeyword(String normalizedText, String keyword) {
        String normalizedKeyword = normalizeText(keyword);
        if (normalizedKeyword == null) {
            return false;
        }

        String pattern = "(^|[^a-z0-9])" + Pattern.quote(normalizedKeyword) + "([^a-z0-9]|$)";
        return Pattern.compile(pattern).matcher(normalizedText).find();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");

        return normalized.isEmpty() ? null : normalized;
    }
}

