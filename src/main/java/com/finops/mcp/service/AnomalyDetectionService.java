package com.finops.mcp.service;

import com.finops.mcp.account.AwsAccountProperties;
import com.finops.mcp.aws.AwsCostExplorerAdapter;
import com.finops.mcp.model.CostAnomaly;
import com.finops.mcp.model.CostRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnomalyDetectionService {

    private static final double DEFAULT_THRESHOLD = 2.0;

    private final AwsCostExplorerAdapter aws;
    private final AwsAccountProperties accountProperties;

    public AnomalyDetectionService(AwsCostExplorerAdapter aws, AwsAccountProperties accountProperties) {
        this.aws = aws;
        this.accountProperties = accountProperties;
    }

    /**
     * Detecta días con coste anómalo por cuenta y servicio, sobre una ventana de {@code days} días.
     * Una anomalía es un día cuyo coste supera media + threshold * desviación estándar.
     */
    public List<CostAnomaly> detectAnomalies(int days, double threshold) {

        List<CostRecord> records = accountProperties.accounts().stream()
                .flatMap(account -> aws.fetchDailyCosts(account, days).stream())
                .toList();

        Map<String, List<CostRecord>> byAccountAndService = records.stream()
                .collect(Collectors.groupingBy(r -> r.accountAlias() + "/" + r.service()));

        List<CostAnomaly> anomalies = new ArrayList<>();

        for (List<CostRecord> group : byAccountAndService.values()) {

            if (group.size() < 3) {
                // Datos insuficientes para calcular desviación de forma fiable
                continue;
            }

            double mean = mean(group);
            double stdDev = stdDev(group, mean);
            double upperBound = mean + threshold * stdDev;

            for (CostRecord record : group) {
                if (record.cost() > upperBound && stdDev > 0) {

                    double deviationPercent = mean == 0
                            ? 100.0
                            : ((record.cost() - mean) / mean) * 100.0;

                    anomalies.add(new CostAnomaly(
                            record.service(),
                            record.date(),
                            record.cost(),
                            mean,
                            deviationPercent,
                            record.accountAlias()
                    ));
                }
            }
        }

        return anomalies.stream()
                .sorted(Comparator.comparingDouble(CostAnomaly::deviationPercent).reversed())
                .toList();
    }

    public List<CostAnomaly> detectAnomalies(int days) {
        return detectAnomalies(days, DEFAULT_THRESHOLD);
    }

    private double mean(List<CostRecord> records) {
        return records.stream()
                .mapToDouble(CostRecord::cost)
                .average()
                .orElse(0.0);
    }

    private double stdDev(List<CostRecord> records, double mean) {
        double variance = records.stream()
                .mapToDouble(r -> Math.pow(r.cost() - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }
}