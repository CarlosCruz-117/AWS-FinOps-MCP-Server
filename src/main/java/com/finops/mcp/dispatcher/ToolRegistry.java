package com.finops.mcp.dispatcher;

import com.finops.mcp.tools.FinOpsTool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ToolRegistry {

    private final Map<String, FinOpsTool> tools = new HashMap<>();

    public ToolRegistry(List<FinOpsTool> toolList) {
        toolList.forEach(t -> tools.put(t.name(), t));
    }

    public FinOpsTool get(String name) {
        return tools.get(name);
    }
}