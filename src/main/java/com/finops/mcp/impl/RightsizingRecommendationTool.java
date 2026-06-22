package com.finops.mcp.impl;

import com.finops.mcp.model.RightsizingSuggestion;
import com.finops.mcp.service.RightsizingService;
import com.finops.mcp.tools.FinOpsTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RightsizingRecommendationTool implements FinOpsTool {

    public static final String GET_RIGHTSIZING_RECOMMENDATIONS = "get_rightsizing_recommendations";

    private final RightsizingService rightsizingService;

    public RightsizingRecommendationTool(RightsizingService rightsizingService) {
        this.rightsizingService = rightsizingService;
    }

    @Override
    public String name() {
        return GET_RIGHTSIZING_RECOMMENDATIONS;
    }

    @Override
    public Object execute(Map<String, Object> args) {

        List<RightsizingSuggestion> suggestions = rightsizingService.getRecommendations();

        return Map.of(
                "suggestions", suggestions,
                "count", suggestions.size(),
                "source", suggestions.isEmpty()
                        ? "NONE"
                        : suggestions.getFirst().source()
        );
    }
}