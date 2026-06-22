package com.finops.mcp.integration;

import com.finops.mcp.model.McpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests para McpController.
 * Verifican la capa HTTP sin levantar clientes AWS reales
 * (los servicios mockean los adapters en el contexto de test).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpControllerITTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnErrorForUnknownTool() {

        McpRequest request = new McpRequest("invalid_tool", Map.of());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/mcp/tools/cost/report", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("ERROR");
        assertThat(response.getBody()).contains("Tool not found");
    }
}
