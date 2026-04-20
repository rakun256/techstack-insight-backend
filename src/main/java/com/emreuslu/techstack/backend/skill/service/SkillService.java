package com.emreuslu.techstack.backend.skill.service;

import com.emreuslu.techstack.backend.common.exception.ResourceNotFoundException;
import com.emreuslu.techstack.backend.skill.dto.CreateSkillRequestDto;
import com.emreuslu.techstack.backend.skill.dto.SkillResponseDto;
import com.emreuslu.techstack.backend.skill.entity.Skill;
import com.emreuslu.techstack.backend.skill.repository.SkillRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    @Transactional
    public SkillResponseDto createSkill(CreateSkillRequestDto request) {
        Skill skill = findOrCreateByName(request.name());
        return toResponseDto(skill);
    }

    @Transactional(readOnly = true)
    public List<SkillResponseDto> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SkillResponseDto getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
        return toResponseDto(skill);
    }

    @Transactional
    public Skill findOrCreateByName(String name) {
        String normalizedName = normalizeName(name);

        return skillRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> skillRepository.save(Skill.builder()
                        .name(normalizedName)
                        .build()));
    }

    private SkillResponseDto toResponseDto(Skill skill) {
        return new SkillResponseDto(
                skill.getId(),
                skill.getName(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }

    private String normalizeName(String rawName) {
        // Keep normalization intentionally simple until extraction rules are introduced.
        return rawName.trim().replaceAll("\\s+", " ");
    }
}

