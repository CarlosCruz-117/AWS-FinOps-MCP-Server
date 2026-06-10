package com.finops.mcp.impl;

import com.finops.mcp.csv.CsvExporter;
import com.finops.mcp.model.CostRecord;
import com.finops.mcp.service.CostAggregationService;
import com.finops.mcp.tools.FinOpsTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TopEc2CostTool implements FinOpsTool {

    public static final String GET_TOP_EC_2_COSTS_LAST_7_DAYS = "get_top_ec2_costs_last_7_days";
    private final CostAggregationService service;
    private final CsvExporter csvExporter;

    public TopEc2CostTool(
            CostAggregationService service,
            CsvExporter csvExporter
    ) {
        this.service = service;
        this.csvExporter = csvExporter;
    }

    @Override
    public String name() {
        return GET_TOP_EC_2_COSTS_LAST_7_DAYS;
    }

    @Override
    public Object execute(Map<String, Object> args) {

        int limit = args.getOrDefault("limit", 10) instanceof Integer i
                ? i
                : 10;

        List<CostRecord> records =
                service.getTopCosts(limit);

        String csvPath =
                csvExporter.export(records);

        return Map.of(
                "records", records,
                "csvFile", csvPath
        );
    }
}
