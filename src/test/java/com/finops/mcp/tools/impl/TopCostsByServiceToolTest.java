package com.finops.mcp.tools.impl;

import com.finops.mcp.csv.CsvExporter;
import com.finops.mcp.impl.TopCostsByServiceTool;
import com.finops.mcp.model.CostRecord;
import com.finops.mcp.service.CostAggregationService;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TopCostsByServiceToolTest {

    @Test
    void shouldReturnAggregatedCostsAndCsvPath() {

        // ARRANGE
        CostAggregationService service =
                mock(CostAggregationService.class);

        CsvExporter csvExporter =
                mock(CsvExporter.class);

        List<CostRecord> mockRecords = List.of(
                new CostRecord(
                        "ec2",
                        "t3.medium",
                        "eu-west-1",
                        99.0
                )
        );

        when(service.getTopCosts(10))
                .thenReturn(mockRecords);

        when(csvExporter.export(mockRecords))
                .thenReturn("output/cost-report.csv");

        TopCostsByServiceTool tool =
                new TopCostsByServiceTool(service, csvExporter);

        // ACT
        Object result =
                tool.execute(Map.of("limit", 10));

        // ASSERT
        assertThat(result)
                .isInstanceOf(Map.class);

        Map<String, Object> response = (Map<String, Object>) result;

        assertThat(response).containsKey("records");

        assertThat(response).containsKey("csvFile");

        assertThat(response.get("csvFile"))
                .isEqualTo("output/cost-report.csv");

        verify(service)
                .getTopCosts(10);

        verify(csvExporter)
                .export(mockRecords);
    }

    @Test
    void shouldUseDefaultLimit_whenLimitNotProvided() {

        // ARRANGE
        CostAggregationService service =
                mock(CostAggregationService.class);

        CsvExporter csvExporter =
                mock(CsvExporter.class);

        when(service.getTopCosts(10))
                .thenReturn(List.of());

        when(csvExporter.export(anyList()))
                .thenReturn("output/cost-report.csv");

        TopCostsByServiceTool tool =
                new TopCostsByServiceTool(service, csvExporter);

        // ACT
        tool.execute(Map.of());

        // ASSERT
        verify(service)
                .getTopCosts(10);

        verify(csvExporter)
                .export(anyList());
    }
}
