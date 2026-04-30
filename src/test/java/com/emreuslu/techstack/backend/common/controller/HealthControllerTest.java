package com.emreuslu.techstack.backend.common.controller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;
@DisplayName("HealthController Unit Tests")
class HealthControllerTest {
    private final HealthController healthController = new HealthController();
    @Test
    @DisplayName("GET / should return 200 OK with welcome message")
    void testGetRootEndpoint() {
        ResponseEntity<String> response = healthController.getRootEndpoint();
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Welcome to TechStack Insight API. The service is proudly running");
    }
    @Test
    @DisplayName("HEAD / should return 200 OK without body")
    void testHeadRootEndpoint() {
        ResponseEntity<Void> response = healthController.headRootEndpoint();
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
