package com.emreuslu.techstack.backend.common.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Hidden
public class HealthController {

    @GetMapping
    public ResponseEntity<String> getRootEndpoint() {
        return ResponseEntity.ok("Welcome to TechStack Insight API. The service is proudly running");
    }

    @RequestMapping(method = RequestMethod.HEAD)
    public ResponseEntity<Void> headRootEndpoint() {
        return ResponseEntity.ok().build();
    }
}


