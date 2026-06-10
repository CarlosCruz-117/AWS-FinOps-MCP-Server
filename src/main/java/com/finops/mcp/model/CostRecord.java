package com.finops.mcp.model;

public record CostRecord(
        String service,
        String usageType,
        String region,
        double cost
) {}