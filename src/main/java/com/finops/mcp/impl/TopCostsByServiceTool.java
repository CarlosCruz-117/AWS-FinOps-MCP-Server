package com.finops.mcp.impl;

import com.finops.mcp.csv.CsvExporter;
import com.finops.mcp.model.CostRecord;
import com.finops.mcp.service.CostAggregationService;
import com.finops.mcp.tools.FinOpsTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TopCostsByServiceTool implements FinOpsTool {

    public static final String GET_TOP_COSTS_BY_SERVICE = "get_top_costs_by_service";

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_DAYS = 7;

    private final CostAggregationService service;
    private final CsvExporter csvExporter;

    public TopCostsByServiceTool(
            CostAggregationService service,
            CsvExporter csvExporter
    ) {
        this.service = service;
        this.csvExporter = csvExporter;
    }

    @Override
    public String name() {
        return GET_TOP_COSTS_BY_SERVICE;
    }

    @Override
    public Object execute(Map<String, Object> args) {

        int limit = intArg(args, "limit", DEFAULT_LIMIT);
        int days = intArg(args, "days", DEFAULT_DAYS);
        String serviceFilter = (String) args.get("service");

        List<CostRecord> records = (serviceFilter != null && !serviceFilter.isBlank())
                ? service.getTopCostsForService(serviceFilter, days, limit)
                : service.getTopCosts(days, limit);

        String csvPath = csvExporter.export(records);

        return Map.of(
                "records", records,
                "csvFile", csvPath
        );
    }

    private int intArg(Map<String, Object> args, String key, int defaultValue) {
        Object value = args.get(key);
        return value instanceof Integer i ? i : defaultValue;
    }
}