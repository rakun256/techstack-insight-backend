package com.emreuslu.techstack.backend.ingestion.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class TextNormalizationService {

    public String normalizeTitle(String rawTitle) {
        return clean(rawTitle);
    }

    public String toPlainText(String rawText) {
        String cleaned = clean(rawText);
        if (cleaned == null) {
            return null;
        }

        String htmlDecoded = HtmlUtils.htmlUnescape(cleaned);
        String withoutTags = htmlDecoded.replaceAll("<[^>]+>", " ");
        return clean(withoutTags);
    }

    public String mergeSections(List<String> sections) {
        if (sections == null || sections.isEmpty()) {
            return null;
        }

        Set<String> uniqueSections = new LinkedHashSet<>();
        for (String section : sections) {
            String cleaned = toPlainText(section);
            if (cleaned != null) {
                uniqueSections.add(cleaned);
            }
        }

        if (uniqueSections.isEmpty()) {
            return null;
        }

        return String.join("\n", uniqueSections);
    }

    public String clean(String value) {
        if (value == null) {
            return null;
        }

        String collapsed = value
                .replace('\u00A0', ' ')
                .trim()
                .replaceAll("\\s+", " ");

        return collapsed.isEmpty() ? null : collapsed;
    }

    public List<String> nonNullSections(String... sections) {
        List<String> list = new ArrayList<>();
        if (sections == null) {
            return list;
        }

        for (String section : sections) {
            if (section != null) {
                list.add(section);
            }
        }
        return list;
    }
}

