package com.finops.mcp.aws;

import com.finops.mcp.account.AwsAccountProperties.AwsAccountConfig;
import com.finops.mcp.account.AwsClientFactory;
import com.finops.mcp.model.CostRecord;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AwsCostExplorerAdapter {

    private static final String METRIC_UNBLENDED_COST = "UnblendedCost";

    private final AwsClientFactory clientFactory;

    public AwsCostExplorerAdapter(AwsClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public List<CostRecord> fetchDailyCosts(AwsAccountConfig account, int days) {
        return fetchDailyCosts(account, days, null);
    }

    public List<CostRecord> fetchDailyCostsForService(AwsAccountConfig account, String awsServiceName, int days) {
        return fetchDailyCosts(account, days, awsServiceName);
    }

    /**
     * Nota sobre: try (CostExplorerClient client = ...) —
     * <p>
     * Los clientes del SDK v2 son AutoCloseable, y como ahora se crean por petición (uno por cuenta),
     * hay que cerrarlos para no acumular conexiones. Si el rendimiento se vuelve un problema con muchas cuentas,
     * el siguiente paso sería cachear los clientes por cuenta en lugar de crear/cerrar en cada llamada —
     * pero para el alcance actual, crear/cerrar es lo más simple y correcto.
     */
    private List<CostRecord> fetchDailyCosts(AwsAccountConfig account, int days, String serviceFilter) {

        try (CostExplorerClient client = clientFactory.costExplorerClient(account)) {

            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(days);

            GetCostAndUsageRequest.Builder requestBuilder = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(start.toString())
                            .end(end.toString())
                            .build())
                    .granularity(Granularity.DAILY)
                    .metrics(METRIC_UNBLENDED_COST)
                    .groupBy(GroupDefinition.builder()
                            .type("DIMENSION")
                            .key("SERVICE")
                            .build());

            if (serviceFilter != null && !serviceFilter.isBlank()) {
                requestBuilder.filter(Expression.builder()
                        .dimensions(DimensionValues.builder()
                                .key(Dimension.SERVICE)
                                .values(serviceFilter)
                                .build())
                        .build());
            }

            GetCostAndUsageResponse response = client.getCostAndUsage(requestBuilder.build());

            return toCostRecords(response, account.alias());
        }
    }

    private List<CostRecord> toCostRecords(GetCostAndUsageResponse response, String accountAlias) {

        List<CostRecord> result = new ArrayList<>();

        for (ResultByTime r : response.resultsByTime()) {

            LocalDate date = LocalDate.parse(r.timePeriod().start());

            for (Group g : r.groups()) {

                String service = g.keys().getFirst();
                double cost = Double.parseDouble(
                        g.metrics().get(METRIC_UNBLENDED_COST).amount()
                );

                result.add(new CostRecord(
                        service,
                        "SERVICE_GROUP",
                        "GLOBAL",
                        cost,
                        date,
                        accountAlias
                ));
            }
        }

        return result;
    }
}