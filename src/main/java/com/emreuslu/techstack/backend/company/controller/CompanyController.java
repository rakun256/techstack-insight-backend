package com.emreuslu.techstack.backend.company.controller;

import com.emreuslu.techstack.backend.company.dto.CompanyResponseDto;
import com.emreuslu.techstack.backend.company.service.CompanyService;
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
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponseDto> getCompanyById(@PathVariable UUID companyId) {
        return ResponseEntity.ok(companyService.getById(companyId));
    }
}

