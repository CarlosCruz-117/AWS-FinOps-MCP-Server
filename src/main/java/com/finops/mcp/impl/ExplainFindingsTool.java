package com.finops.mcp.impl;

import com.finops.mcp.model.CostAnomaly;
import com.finops.mcp.model.RightsizingSuggestion;
import com.finops.mcp.service.AnomalyDetectionService;
import com.finops.mcp.service.RightsizingService;
import com.finops.mcp.tools.FinOpsTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExplainFindingsTool implements FinOpsTool {

    public static final String EXPLAIN_FINDINGS = "explain_findings";

    private static final int DEFAULT_DAYS = 14;
    private static final double DEFAULT_THRESHOLD = 2.0;

    private final AnomalyDetectionService anomalyDetectionService;
    private final RightsizingService rightsizingService;

    public ExplainFindingsTool(AnomalyDetectionService anomalyDetectionService,
                               RightsizingService rightsizingService) {
        this.anomalyDetectionService = anomalyDetectionService;
        this.rightsizingService = rightsizingService;
    }

    @Override
    public String name() {
        return EXPLAIN_FINDINGS;
    }

    @Override
    public Object execute(Map<String, Object> args) {

        int days = intArg(args, "days", DEFAULT_DAYS);
        double threshold = doubleArg(args, "threshold", DEFAULT_THRESHOLD);

        List<CostAnomaly> anomalies = anomalyDetectionService.detectAnomalies(days, threshold);
        List<RightsizingSuggestion> suggestions = rightsizingService.getRecommendations();

        String explanation = buildExplanation(anomalies, suggestions);

        return Map.of(
                "explanation", explanation,
                "anomalyCount", anomalies.size(),
                "rightsizingCount", suggestions.size()
        );
    }

    private String buildExplanation(List<CostAnomaly> anomalies,
                                    List<RightsizingSuggestion> suggestions) {

        StringBuilder sb = new StringBuilder();
        sb.append("## AWS FinOps Analysis\n\n");

        // --- Anomalías ---
        sb.append("### Cost Anomalies\n");
        if (anomalies.isEmpty()) {
            sb.append("No cost anomalies detected in the analysis period.\n");
        } else {
            Map<String, List<CostAnomaly>> byAccount =
                    anomalies.stream().collect(Collectors.groupingBy(CostAnomaly::accountAlias));

            byAccount.forEach((account, accountAnomalies) -> {
                sb.append("\n**Account: %s**\n".formatted(account));
                accountAnomalies.forEach(a ->
                        sb.append("- `%s` on %s: $%.2f actual vs $%.2f expected (+%.1f%% deviation)\n"
                                .formatted(
                                        a.service(),
                                        a.date(),
                                        a.actualCost(),
                                        a.expectedCost(),
                                        a.deviationPercent()
                                ))
                );
            });
        }

        // --- Rightsizing ---
        sb.append("\n### Rightsizing Recommendations\n");
        if (suggestions.isEmpty()) {
            sb.append("No rightsizing recommendations available.\n");
        } else {
            Map<String, List<RightsizingSuggestion>> byAccount =
                    suggestions.stream().collect(Collectors.groupingBy(RightsizingSuggestion::accountAlias));

            byAccount.forEach((account, accountSuggestions) -> {
                sb.append("\n**Account: %s**\n".formatted(account));
                double totalSavings = accountSuggestions.stream()
                        .mapToDouble(RightsizingSuggestion::estimatedMonthlySavings)
                        .sum();

                accountSuggestions.forEach(s ->
                        sb.append("- `%s` (%s → %s): %s [source: %s]\n"
                                .formatted(
                                        s.resourceId(),
                                        s.currentType(),
                                        s.recommendedType(),
                                        s.reason(),
                                        s.source()
                                ))
                );

                if (totalSavings > 0) {
                    sb.append("  **Estimated monthly savings: $%.2f**\n".formatted(totalSavings));
                }
            });
        }

        return sb.toString();
    }

    private int intArg(Map<String, Object> args, String key, int defaultValue) {
        Object value = args.get(key);
        return value instanceof Integer i ? i : defaultValue;
    }

    private double doubleArg(Map<String, Object> args, String key, double defaultValue) {
        Object value = args.get(key);
        return value instanceof Number n ? n.doubleValue() : defaultValue;
    }
}