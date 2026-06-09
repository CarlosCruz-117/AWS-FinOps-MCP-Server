package com.finops.mcp.controller;

import com.finops.mcp.dispatcher.ToolDispatcher;
import com.finops.mcp.model.McpRequest;
import com.finops.mcp.model.McpResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private final ToolDispatcher dispatcher;

    public McpController(ToolDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping("/tools/call")
    public McpResponse call(@RequestBody McpRequest request) {
        return dispatcher.dispatch(request);
    }
}