package java.com.finops.mcp.integration;

import com.finops.mcp.model.McpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * INTEGRATION TEST
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCallToolSuccessfully() {

        McpRequest request = new McpRequest(
                "get_top_ec2_costs_last_7_days",
                Map.of("limit", 2)
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/mcp/tools/call",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("OK");
    }

    @Test
    void shouldReturnErrorForUnknownTool() {

        McpRequest request = new McpRequest(
                "invalid_tool",
                Map.of()
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/mcp/tools/call",
                request,
                String.class
        );

        assertThat(response.getBody()).contains("ERROR");
    }
}
