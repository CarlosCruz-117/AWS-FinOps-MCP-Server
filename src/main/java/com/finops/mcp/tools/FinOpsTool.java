package com.finops.mcp.tools;

import java.util.Map;

public interface FinOpsTool {
    String name();
    Object execute(Map<String, Object> args);
}