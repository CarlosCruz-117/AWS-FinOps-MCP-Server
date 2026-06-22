package com.finops.mcp.model;

import java.time.LocalDate;

public record CostAnomaly(
        String service,
        LocalDate date,
        double actualCost,
        double expectedCost,
        double deviationPercent,
        String accountAlias
) {
}