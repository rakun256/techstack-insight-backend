package com.emreuslu.techstack.backend.skill.controller;

import com.emreuslu.techstack.backend.skill.dto.CreateSkillRequestDto;
import com.emreuslu.techstack.backend.skill.dto.SkillResponseDto;
import com.emreuslu.techstack.backend.skill.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "Skill read/write operations")
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @Operation(summary = "Create skill or return existing skill by normalized name")
    public ResponseEntity<SkillResponseDto> createSkill(@Valid @RequestBody CreateSkillRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.createSkill(request));
    }

    @GetMapping
    @Operation(summary = "List all skills")
    public ResponseEntity<List<SkillResponseDto>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by id")
    public ResponseEntity<SkillResponseDto> getSkillById(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getSkillById(id));
    }
}

