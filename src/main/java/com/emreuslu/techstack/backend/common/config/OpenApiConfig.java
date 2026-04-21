package com.emreuslu.techstack.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI techStackInsightOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TechStack Insight API")
                        .version("v1")
                        .description("Multi-source job market analysis backend API"));
    }
}

