package com.emreuslu.techstack.backend.skill.service;

import com.emreuslu.techstack.backend.skill.entity.Skill;
import com.emreuslu.techstack.backend.skill.entity.SkillAlias;
import com.emreuslu.techstack.backend.skill.repository.SkillAliasRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillAliasResolutionTest {

    @Mock
    private SkillAliasRepository skillAliasRepository;

    @InjectMocks
    private SkillAliasService skillAliasService;

    private SkillAlias jsAlias;

    @BeforeEach
    void setUp() {
        Skill canonicalSkill = Skill.builder().id(1L).name("JavaScript").build();
        jsAlias = SkillAlias.builder()
                .id(1L)
                .canonicalSkill(canonicalSkill)
                .aliasName("js")
                .build();
    }

    @Test
    void resolveAlias_whenAliasExists_returnCanonicalSkill() {
        when(skillAliasRepository.findByAliasName("js")).thenReturn(Optional.of(jsAlias));

        Optional<Skill> result = skillAliasService.resolveAlias("js");

        assertTrue(result.isPresent());
        assertEquals("JavaScript", result.get().getName());
        verify(skillAliasRepository, times(1)).findByAliasName("js");
    }

    @Test
    void resolveAlias_whenAliasDoesNotExist_returnEmpty() {
        when(skillAliasRepository.findByAliasName("unknown")).thenReturn(Optional.empty());

        Optional<Skill> result = skillAliasService.resolveAlias("unknown");

        assertFalse(result.isPresent());
        verify(skillAliasRepository, times(1)).findByAliasName("unknown");
    }

    @Test
    void resolveAlias_normalizesInput() {
        when(skillAliasRepository.findByAliasName("js")).thenReturn(Optional.of(jsAlias));

        Optional<Skill> result = skillAliasService.resolveAlias("  js  ");

        assertTrue(result.isPresent());
        assertEquals("JavaScript", result.get().getName());
    }
}

