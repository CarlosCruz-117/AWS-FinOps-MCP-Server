package com.finops.mcp.dispatcher;

import com.finops.mcp.model.McpRequest;
import com.finops.mcp.model.McpResponse;
import com.finops.mcp.tools.FinOpsTool;
import org.springframework.stereotype.Service;

@Service
public class ToolDispatcher {

    private final ToolRegistry registry;

    public ToolDispatcher(ToolRegistry registry) {
        this.registry = registry;
    }

    public McpResponse dispatch(McpRequest request) {

        FinOpsTool tool = registry.get(request.tool());

        if (tool == null) {
            return McpResponse.error("Tool not found: " + request.tool());
        }

        try {
            return McpResponse.ok(tool.execute(request.arguments()));
        } catch (Exception e) {
            return McpResponse.error(e.getMessage());
        }
    }
}