package com.finops.mcp.service;

import com.finops.mcp.aws.AwsCostExplorerAdapter;
import com.finops.mcp.model.CostRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CostAggregationServiceTest {

    @Test
    void shouldReturnTopNCostsSorted() {

        AwsCostExplorerAdapter aws = mock(AwsCostExplorerAdapter.class);

        when(aws.fetchLast7DaysEc2Costs()).thenReturn(List.of(
                new CostRecord("ec2", "t3", "eu-west-1", 10),
                new CostRecord("ec2", "m5", "eu-west-1", 50),
                new CostRecord("ec2", "t2", "eu-west-1", 20)
        ));

        CostAggregationService service = new CostAggregationService(aws);

        var result = service.getTopCosts(2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).cost()).isEqualTo(50);
        assertThat(result.get(1).cost()).isEqualTo(20);
    }

    @Test
    void shouldHandleEmptyData() {

        AwsCostExplorerAdapter aws = mock(AwsCostExplorerAdapter.class);

        when(aws.fetchLast7DaysEc2Costs()).thenReturn(List.of());

        CostAggregationService service = new CostAggregationService(aws);

        var result = service.getTopCosts(5);

        assertThat(result).isEmpty();
    }
}
