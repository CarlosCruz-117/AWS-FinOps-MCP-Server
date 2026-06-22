package com.finops.mcp.service;

import com.finops.mcp.account.AwsAccountProperties;
import com.finops.mcp.account.AwsAccountProperties.AwsAccountConfig;
import com.finops.mcp.aws.AwsCostExplorerAdapter;
import com.finops.mcp.model.CostRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CostAggregationServiceTest {

    private final AwsAccountConfig account =
            new AwsAccountConfig("111111111111", "test-account", "", "eu-west-1");

    private final AwsAccountProperties props =
            new AwsAccountProperties(List.of(account));

    @Test
    void shouldReturnTopNCostsSortedDescending() {

        AwsCostExplorerAdapter aws = mock(AwsCostExplorerAdapter.class);
        when(aws.fetchDailyCosts(eq(account), anyInt())).thenReturn(List.of(
                record("EC2", 10.0),
                record("S3", 50.0),
                record("RDS", 20.0)
        ));

        CostAggregationService service = new CostAggregationService(aws, props);

        List<CostRecord> result = service.getTopCosts(7, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).cost()).isEqualTo(50.0);
        assertThat(result.get(1).cost()).isEqualTo(20.0);
    }

    @Test
    void shouldReturnEmpty_whenNoData() {

        AwsCostExplorerAdapter aws = mock(AwsCostExplorerAdapter.class);
        when(aws.fetchDailyCosts(any(), anyInt())).thenReturn(List.of());

        CostAggregationService service = new CostAggregationService(aws, props);

        assertThat(service.getTopCosts(7, 5)).isEmpty();
    }

    @Test
    void shouldAggregateAcrossMultipleAccounts() {

        AwsAccountConfig account2 =
                new AwsAccountConfig("222222222222", "prod", "", "eu-west-1");
        AwsAccountProperties multiProps =
                new AwsAccountProperties(List.of(account, account2));

        AwsCostExplorerAdapter aws = mock(AwsCostExplorerAdapter.class);
        when(aws.fetchDailyCosts(eq(account), anyInt()))
                .thenReturn(List.of(record("EC2", 30.0)));
        when(aws.fetchDailyCosts(eq(account2), anyInt()))
                .thenReturn(List.of(record("EC2", 80.0)));

        CostAggregationService service = new CostAggregationService(aws, multiProps);

        List<CostRecord> result = service.getTopCosts(7, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).cost()).isEqualTo(80.0);
    }

    private CostRecord record(String service, double cost) {
        return new CostRecord(service, "SERVICE_GROUP", "GLOBAL",
                cost, LocalDate.now(), "test-account");
    }
}
