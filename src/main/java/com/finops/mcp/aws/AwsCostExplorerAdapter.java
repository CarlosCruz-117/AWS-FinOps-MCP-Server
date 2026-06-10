package com.finops.mcp.aws;

import com.finops.mcp.model.CostRecord;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AwsCostExplorerAdapter {

    private final CostExplorerClient client;

    public AwsCostExplorerAdapter(CostExplorerClient client) {
        this.client = client;
    }

    public List<CostRecord> fetchLast7DaysEc2Costs() {

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);

        GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                .timePeriod(DateInterval.builder()
                        .start(start.toString())
                        .end(end.toString())
                        .build())
                .granularity(Granularity.DAILY)
                .metrics("UnblendedCost")
                .groupBy(
                        GroupDefinition.builder()
                                .type("DIMENSION")
                                .key("SERVICE")
                                .build()
                )
                .build();

        GetCostAndUsageResponse response = client.getCostAndUsage(request);

        List<CostRecord> result = new ArrayList<>();

        for (ResultByTime r : response.resultsByTime()) {
            for (Group g : r.groups()) {

                String service = g.keys().getFirst();
                double cost = Double.parseDouble(
                        g.metrics().get("UnblendedCost").amount()
                );

                result.add(new CostRecord(
                        service,
                        "SERVICE_GROUP",
                        "GLOBAL",
                        cost
                ));
            }
        }

        return result;
    }
}
