package com.finops.mcp.model;

/**
 * El campo "source" en la respuesta es deliberado:
 * en una demo, poder decir "esto vino de la API oficial de AWS" vs "esto lo calculamos nosotros con CloudWatch"
 */
public record RightsizingSuggestion(
        String resourceId,
        String currentType,
        String recommendedType,
        double estimatedMonthlySavings,
        String reason,
        String source // "AWS_RIGHTSIZING_API" o "CPU_UTILIZATION_HEURISTIC"
) {
}