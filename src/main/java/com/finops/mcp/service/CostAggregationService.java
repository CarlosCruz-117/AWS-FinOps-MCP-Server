package com.finops.mcp.service;

import com.finops.mcp.aws.AwsCostExplorerAdapter;
import com.finops.mcp.model.CostRecord;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CostAggregationService {

    private final AwsCostExplorerAdapter aws;

    public CostAggregationService(AwsCostExplorerAdapter aws) {
        this.aws = aws;
    }

    public List<CostRecord> getTopCosts(int limit) {

        return aws.fetchLast7DaysEc2Costs()
                .stream()
                .sorted(Comparator.comparingDouble(CostRecord::cost).reversed())
                .limit(limit)
                .toList();
    }
}
