package com.finops.mcp.service;

import com.finops.mcp.account.AwsAccountProperties;
import com.finops.mcp.aws.AwsCostExplorerAdapter;
import com.finops.mcp.model.CostRecord;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CostAggregationService {

    private final AwsCostExplorerAdapter aws;
    private final AwsAccountProperties accountProperties;

    public CostAggregationService(AwsCostExplorerAdapter aws, AwsAccountProperties accountProperties) {
        this.aws = aws;
        this.accountProperties = accountProperties;
    }

    public List<CostRecord> getTopCosts(int days, int limit) {
        return accountProperties.accounts().stream()
                .flatMap(account -> aws.fetchDailyCosts(account, days).stream())
                .sorted(Comparator.comparingDouble(CostRecord::cost).reversed())
                .limit(limit)
                .toList();
    }

    public List<CostRecord> getTopCostsForService(String awsServiceName, int days, int limit) {
        return accountProperties.accounts().stream()
                .flatMap(account -> aws.fetchDailyCostsForService(account, awsServiceName, days).stream())
                .sorted(Comparator.comparingDouble(CostRecord::cost).reversed())
                .limit(limit)
                .toList();
    }
}