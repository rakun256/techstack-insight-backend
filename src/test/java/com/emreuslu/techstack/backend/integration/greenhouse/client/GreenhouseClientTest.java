package com.emreuslu.techstack.backend.integration.greenhouse.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.emreuslu.techstack.backend.integration.greenhouse.dto.GreenhouseJobResponseDto;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GreenhouseClientTest {

    private HttpServer server;
    private GreenhouseClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/boards/vercel/jobs", new JobsHandler());
        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        client = new GreenhouseClient(baseUrl, "/v1/boards/{boardToken}/jobs");
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void fetchJobsParsesHeterogeneousMetadataValues() {
        List<GreenhouseJobResponseDto> jobs = client.fetchJobs("vercel");

        assertThat(jobs).hasSize(4);
        assertThat(jobs.get(0).metadata().get(0).value().isTextual()).isTrue();
        assertThat(jobs.get(1).metadata().get(0).value().isObject()).isTrue();
        assertThat(jobs.get(2).metadata().get(0).value().isArray()).isTrue();
        assertThat(jobs.get(3).metadata().get(0).value().isNull()).isTrue();
    }

    private static class JobsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                    {
                      "jobs": [
                        {
                          "id": 1,
                          "title": "Backend Engineer",
                          "metadata": [
                            {"name": "Level", "value": "Senior"}
                          ]
                        },
                        {
                          "id": 2,
                          "title": "Backend Engineer",
                          "metadata": [
                            {"name": "Stack", "value": {"primary": "Java", "secondary": ["Spring", "PostgreSQL"]}}
                          ]
                        },
                        {
                          "id": 3,
                          "title": "Backend Engineer",
                          "metadata": [
                            {"name": "Tags", "value": ["API", "Data"]}
                          ]
                        },
                        {
                          "id": 4,
                          "title": "Backend Engineer",
                          "metadata": [
                            {"name": "Unknown", "value": null}
                          ]
                        }
                      ]
                    }
                    """;

            byte[] payload = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(payload);
            }
        }
    }
}

