package com.finops.mcp.model;

public record McpResponse(
        String status,
        Object data,
        String error
) {
    public static McpResponse ok(Object data) {
        return new McpResponse("OK", data, null);
    }

    public static McpResponse error(String error) {
        return new McpResponse("ERROR", null, error);
    }
}