package com.finops.mcp.service;

import com.finops.mcp.aws.AwsRightsizingAdapter;
import com.finops.mcp.model.RightsizingSuggestion;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.model.RightsizingRecommendation; // <- Corregido aquí
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RightsizingService {

    private static final double LOW_CPU_THRESHOLD = 15.0; // %
    private static final int DEFAULT_DAYS = 14;

    // El DOWNSIZE_MAP es deliberadamente simple y solo cubre familias t3/m5
    private static final Map<String, String> DOWNSIZE_MAP = Map.of(
            "t3.2xlarge", "t3.xlarge",
            "t3.xlarge", "t3.large",
            "t3.large", "t3.medium",
            "t3.medium", "t3.small",
            "m5.xlarge", "m5.large",
            "m5.large", "m5.medium"
    );

    private final AwsRightsizingAdapter adapter;

    public RightsizingService(AwsRightsizingAdapter adapter) {
        this.adapter = adapter;
    }

    public List<RightsizingSuggestion> getRecommendations() {

        List<RightsizingSuggestion> awsRecommendations = fromAwsApi();

        if (!awsRecommendations.isEmpty()) {
            return awsRecommendations;
        }

        return fromCpuHeuristic();
    }

    private List<RightsizingSuggestion> fromAwsApi() {

        List<RightsizingSuggestion> result = new ArrayList<>();

        // Corregido el tipo de objeto en el bucle for
        for (RightsizingRecommendation entry : adapter.fetchAwsRightsizingRecommendations()) {

            if (entry.modifyRecommendationDetail() == null
                    || entry.modifyRecommendationDetail().targetInstances().isEmpty()) {
                continue;
            }

            var current = entry.currentInstance();
            var target = entry.modifyRecommendationDetail().targetInstances().getFirst();

            double savings = current.monthlyCost() != null
                    ? Double.parseDouble(current.monthlyCost())
                      - Double.parseDouble(target.estimatedMonthlyCost())
                    : 0.0;

            result.add(new RightsizingSuggestion(
                    current.resourceId(),
                    current.resourceDetails().ec2ResourceDetails().instanceType(),
                    target.resourceDetails().ec2ResourceDetails().instanceType(),
                    savings,
                    "AWS Cost Explorer recommendation based on usage history",
                    "AWS_RIGHTSIZING_API"
            ));
        }

        return result;
    }

    private List<RightsizingSuggestion> fromCpuHeuristic() {

        List<RightsizingSuggestion> result = new ArrayList<>();

        for (Instance instance : adapter.fetchRunningInstances()) {

            String currentType = instance.instanceTypeAsString();
            String recommendedType = DOWNSIZE_MAP.get(currentType);

            if (recommendedType == null) {
                continue; // no tenemos un downsize sugerido para este tipo
            }

            double avgCpu = adapter.fetchAverageCpuUtilization(instance.instanceId(), DEFAULT_DAYS);

            if (avgCpu < 0 || avgCpu >= LOW_CPU_THRESHOLD) {
                continue; // sin datos suficientes o uso normal
            }

            result.add(new RightsizingSuggestion(
                    instance.instanceId(),
                    currentType,
                    recommendedType,
                    0.0, // sin coste real disponible en este fallback
                    "Average CPU utilization %.1f%% over last %d days is below threshold (%.0f%%)"
                            .formatted(avgCpu, DEFAULT_DAYS, LOW_CPU_THRESHOLD),
                    "CPU_UTILIZATION_HEURISTIC"
            ));
        }

        return result;
    }
}