package com.finops.mcp.model;

import java.util.Map;

public record McpRequest(
        String tool,
        Map<String, Object> arguments
) {}