package com.emreuslu.techstack.backend.company.controller;

import com.emreuslu.techstack.backend.company.dto.CompanyResponseDto;
import com.emreuslu.techstack.backend.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company read operations")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{companyId}")
    @Operation(summary = "Get company by id")
    public ResponseEntity<CompanyResponseDto> getCompanyById(@PathVariable UUID companyId) {
        return ResponseEntity.ok(companyService.getById(companyId));
    }
}

