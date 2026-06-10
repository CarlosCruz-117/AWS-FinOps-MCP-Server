package com.finops.mcp.dispatcher;

import com.finops.mcp.model.McpRequest;
import com.finops.mcp.model.McpResponse;
import com.finops.mcp.tools.FinOpsTool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ToolDispatcherTest {

    @Test
    void shouldReturnError_whenToolDoesNotExist() {

        ToolRegistry registry = new ToolRegistry(List.of());
        ToolDispatcher dispatcher = new ToolDispatcher(registry);

        McpRequest request = new McpRequest("unknown_tool", Map.of());

        McpResponse response = dispatcher.dispatch(request);

        assertThat(response.status()).isEqualTo("ERROR");
        assertThat(response.error()).contains("Tool not found");
    }

    @Test
    void shouldExecuteToolSuccessfully() {

        FinOpsTool fakeTool = new FinOpsTool() {
            @Override
            public String name() {
                return "test_tool";
            }

            @Override
            public Object execute(Map<String, Object> args) {
                return "OK_RESULT";
            }
        };

        ToolRegistry registry = new ToolRegistry(List.of(fakeTool));
        ToolDispatcher dispatcher = new ToolDispatcher(registry);

        McpRequest request = new McpRequest("test_tool", Map.of());

        McpResponse response = dispatcher.dispatch(request);

        assertThat(response.status()).isEqualTo("OK");
        assertThat(response.data()).isEqualTo("OK_RESULT");
    }
}
