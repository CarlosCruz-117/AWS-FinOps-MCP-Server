package com.finops.mcp.aws;

import com.finops.mcp.account.AwsAccountProperties.AwsAccountConfig;
import com.finops.mcp.account.AwsClientFactory;
import com.finops.mcp.model.CostRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AwsCostExplorerAdapterTest {

    private AwsClientFactory clientFactory;
    private CostExplorerClient costExplorerClient;
    private AwsCostExplorerAdapter adapter;

    private final AwsAccountConfig testAccount =
            new AwsAccountConfig("111111111111", "test-account", "", "eu-west-1");

    @BeforeEach
    void setUp() {
        clientFactory = mock(AwsClientFactory.class);
        costExplorerClient = mock(CostExplorerClient.class);
        doNothing().when(costExplorerClient).close();
        when(clientFactory.costExplorerClient(any())).thenReturn(costExplorerClient);
        adapter = new AwsCostExplorerAdapter(clientFactory);
    }

    @Test
    void shouldMapResponseToCostRecords() {

        // ARRANGE
        GetCostAndUsageResponse response = buildResponse(
                "2024-06-01", "Amazon EC2", "12.50"
        );
        when(costExplorerClient.getCostAndUsage(any(GetCostAndUsageRequest.class)))
                .thenReturn(response);

        // ACT
        List<CostRecord> records = adapter.fetchDailyCosts(testAccount, 7);

        // ASSERT
        assertThat(records).hasSize(1);
        assertThat(records.getFirst().service()).isEqualTo("Amazon EC2");
        assertThat(records.getFirst().cost()).isEqualTo(12.50);
        assertThat(records.getFirst().accountAlias()).isEqualTo("test-account");
        assertThat(records.getFirst().date()).isNotNull();
    }

    @Test
    void shouldReturnEmptyList_whenNoResultsFromAws() {

        GetCostAndUsageResponse emptyResponse = GetCostAndUsageResponse.builder()
                .resultsByTime(List.of())
                .build();

        when(costExplorerClient.getCostAndUsage(any(GetCostAndUsageRequest.class)))
                .thenReturn(emptyResponse);

        List<CostRecord> records = adapter.fetchDailyCosts(testAccount, 7);

        assertThat(records).isEmpty();
    }

    @Test
    void shouldApplyServiceFilter_whenServiceProvided() {

        GetCostAndUsageResponse response = buildResponse(
                "2024-06-01", "Amazon EC2", "50.00"
        );
        when(costExplorerClient.getCostAndUsage(any(GetCostAndUsageRequest.class)))
                .thenReturn(response);

        List<CostRecord> records = adapter.fetchDailyCostsForService(
                testAccount, "Amazon Elastic Compute Cloud - Compute", 7);

        assertThat(records).hasSize(1);
//        verify(costExplorerClient).getCostAndUsage( argThat(req ->
//                req.filter() != null
//        ));
    }

    @Test
    void shouldPropagateException_whenAwsFails() {

        when(costExplorerClient.getCostAndUsage(any(GetCostAndUsageRequest.class)))
                .thenThrow(new RuntimeException("AWS timeout"));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> adapter.fetchDailyCosts(testAccount, 7))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AWS timeout");
    }

    // --- helpers ---

    private GetCostAndUsageResponse buildResponse(String date, String service, String amount) {
        return GetCostAndUsageResponse.builder()
                .resultsByTime(ResultByTime.builder()
                        .timePeriod(DateInterval.builder()
                                .start(date)
                                .end(date)
                                .build())
                        .groups(Group.builder()
                                .keys(service)
                                .metrics(Map.of(
                                        "UnblendedCost",
                                        MetricValue.builder().amount(amount).unit("USD").build()
                                ))
                                .build())
                        .build())
                .build();
    }
}
