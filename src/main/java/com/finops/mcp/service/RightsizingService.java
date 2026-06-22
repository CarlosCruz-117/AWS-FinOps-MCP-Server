package com.finops.mcp.service;

import com.finops.mcp.account.AwsAccountProperties;
import com.finops.mcp.account.AwsAccountProperties.AwsAccountConfig;
import com.finops.mcp.aws.AwsRightsizingAdapter;
import com.finops.mcp.model.RightsizingSuggestion;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.model.RightsizingRecommendation;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RightsizingService {

    private static final double LOW_CPU_THRESHOLD = 15.0;
    private static final int DEFAULT_DAYS = 14;

    // Mapa de downsizing dentro de la misma familia (cubre casos comunes de portfolio)
    private static final Map<String, String> DOWNSIZE_MAP = Map.of(
            "t3.2xlarge", "t3.xlarge",
            "t3.xlarge", "t3.large",
            "t3.large", "t3.medium",
            "t3.medium", "t3.small",
            "m5.xlarge", "m5.large",
            "m5.large", "m5.medium"
    );

    private final AwsRightsizingAdapter adapter;
    private final AwsAccountProperties accountProperties;

    public RightsizingService(AwsRightsizingAdapter adapter,
                              AwsAccountProperties accountProperties) {
        this.adapter = adapter;
        this.accountProperties = accountProperties;
    }

    public List<RightsizingSuggestion> getRecommendations() {

        List<RightsizingSuggestion> result = new ArrayList<>();

        for (AwsAccountConfig account : accountProperties.accounts()) {
            List<RightsizingSuggestion> fromApi = fromAwsApi(account);
            result.addAll(fromApi.isEmpty() ? fromCpuHeuristic(account) : fromApi);
        }

        return result;
    }

    private List<RightsizingSuggestion> fromAwsApi(AwsAccountConfig account) {

        List<RightsizingSuggestion> result = new ArrayList<>();

        for (RightsizingRecommendation recommendation :
                adapter.fetchAwsRightsizingRecommendations(account)) {

            // Solo procesamos recomendaciones de tipo MODIFY (bajar tamaño)
            // TERMINATE es otro tipo válido que dejamos fuera del scope actual
            if (recommendation.modifyRecommendationDetail() == null
                    || recommendation.modifyRecommendationDetail().targetInstances().isEmpty()) {
                continue;
            }

            var current = recommendation.currentInstance();
            var target = recommendation.modifyRecommendationDetail()
                    .targetInstances()
                    .getFirst();

            double savings = parseSavings(current.monthlyCost(), target.estimatedMonthlyCost());

            result.add(new RightsizingSuggestion(
                    current.resourceId(),
                    current.resourceDetails().ec2ResourceDetails().instanceType(),
                    target.resourceDetails().ec2ResourceDetails().instanceType(),
                    savings,
                    "AWS Cost Explorer recommendation based on usage history",
                    "AWS_RIGHTSIZING_API",
                    account.alias()
            ));
        }

        return result;
    }

    private List<RightsizingSuggestion> fromCpuHeuristic(AwsAccountConfig account) {

        List<RightsizingSuggestion> result = new ArrayList<>();

        for (Instance instance : adapter.fetchRunningInstances(account)) {

            String currentType = instance.instanceTypeAsString();
            String recommendedType = DOWNSIZE_MAP.get(currentType);

            if (recommendedType == null) continue;

            double avgCpu = adapter.fetchAverageCpuUtilization(
                    account, instance.instanceId(), DEFAULT_DAYS);

            if (avgCpu < 0 || avgCpu >= LOW_CPU_THRESHOLD) continue;

            result.add(new RightsizingSuggestion(
                    instance.instanceId(),
                    currentType,
                    recommendedType,
                    0.0,
                    "Average CPU utilization %.1f%% over last %d days is below threshold (%.0f%%)"
                            .formatted(avgCpu, DEFAULT_DAYS, LOW_CPU_THRESHOLD),
                    "CPU_UTILIZATION_HEURISTIC",
                    account.alias()
            ));
        }

        return result;
    }

    private double parseSavings(String currentMonthlyCost, String targetMonthlyCost) {
        try {
            if (currentMonthlyCost == null || targetMonthlyCost == null) return 0.0;
            return Double.parseDouble(currentMonthlyCost)
                    - Double.parseDouble(targetMonthlyCost);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}