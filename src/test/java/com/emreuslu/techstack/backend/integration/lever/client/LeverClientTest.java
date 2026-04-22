package com.emreuslu.techstack.backend.integration.lever.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.emreuslu.techstack.backend.integration.lever.dto.LeverJobResponseDto;
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

class LeverClientTest {

    private HttpServer server;
    private LeverClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v0/postings/plaid", new PostingsHandler());
        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        client = new LeverClient(baseUrl, "/v0/postings/{companyToken}");
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void fetchJobsParsesHeterogeneousListContent() {
        List<LeverJobResponseDto> jobs = client.fetchJobs("plaid");

        assertThat(jobs).hasSize(4);
        assertThat(jobs.get(0).lists().get(0).content().isTextual()).isTrue();
        assertThat(jobs.get(1).lists().get(0).content().isArray()).isTrue();
        assertThat(jobs.get(2).lists().get(0).content().isObject()).isTrue();
        assertThat(jobs.get(3).lists().get(0).content().isNull()).isTrue();
    }

    private static class PostingsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                    [
                      {
                        "id": "job-1",
                        "text": "Backend Engineer",
                        "lists": [
                          {"text": "Responsibilities", "content": "Build APIs"}
                        ]
                      },
                      {
                        "id": "job-2",
                        "text": "Backend Engineer",
                        "lists": [
                          {"text": "Qualifications", "content": ["Java", {"text": "Spring"}]}
                        ]
                      },
                      {
                        "id": "job-3",
                        "text": "Backend Engineer",
                        "lists": [
                          {"text": "Details", "content": {"focus": "Platform", "level": "Senior"}}
                        ]
                      },
                      {
                        "id": "job-4",
                        "text": "Backend Engineer",
                        "lists": [
                          {"text": "Optional", "content": null}
                        ]
                      }
                    ]
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

