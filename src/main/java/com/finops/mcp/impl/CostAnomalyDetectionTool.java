package com.finops.mcp.impl;

import com.finops.mcp.model.CostAnomaly;
import com.finops.mcp.service.AnomalyDetectionService;
import com.finops.mcp.tools.FinOpsTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CostAnomalyDetectionTool implements FinOpsTool {

    public static final String DETECT_COST_ANOMALIES = "detect_cost_anomalies";

    private static final int DEFAULT_DAYS = 14;
    private static final double DEFAULT_THRESHOLD = 2.0;

    private final AnomalyDetectionService anomalyDetectionService;

    public CostAnomalyDetectionTool(AnomalyDetectionService anomalyDetectionService) {
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @Override
    public String name() {
        return DETECT_COST_ANOMALIES;
    }

    @Override
    public Object execute(Map<String, Object> args) {

        int days = intArg(args, "days", DEFAULT_DAYS);
        double threshold = doubleArg(args, "threshold", DEFAULT_THRESHOLD);

        List<CostAnomaly> anomalies =
                anomalyDetectionService.detectAnomalies(days, threshold);

        return Map.of(
                "anomalies", anomalies,
                "count", anomalies.size()
        );
    }

    private int intArg(Map<String, Object> args, String key, int defaultValue) {
        Object value = args.get(key);
        return value instanceof Integer i ? i : defaultValue;
    }

    private double doubleArg(Map<String, Object> args, String key, double defaultValue) {
        Object value = args.get(key);
        if (value instanceof Number n) return n.doubleValue();
        return defaultValue;
    }
}