package com.finops.mcp.tools.impl;

import com.finops.mcp.csv.CsvExporter;
import com.finops.mcp.impl.TopCostsByServiceTool;
import com.finops.mcp.model.CostRecord;
import com.finops.mcp.service.CostAggregationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TopCostsByServiceToolTest {

    private final CostAggregationService service = mock(CostAggregationService.class);
    private final CsvExporter csvExporter = mock(CsvExporter.class);
    private final TopCostsByServiceTool tool = new TopCostsByServiceTool(service, csvExporter);

    @Test
    void shouldReturnRecordsAndCsvPath() {

        List<CostRecord> mockRecords = List.of(
                new CostRecord("EC2", "SERVICE_GROUP", "GLOBAL", 99.0,
                        LocalDate.now(), "test-account")
        );
        when(service.getTopCosts(7, 10)).thenReturn(mockRecords);
        when(csvExporter.export(mockRecords)).thenReturn("output/cost-report.csv");

        Map<String, Object> result = (Map<String, Object>)
                tool.execute(Map.of("limit", 10, "days", 7));

        assertThat(result).containsKeys("records", "csvFile");
        assertThat(result.get("csvFile")).isEqualTo("output/cost-report.csv");
        verify(service).getTopCosts(7, 10);
    }

    @Test
    void shouldUseDefaultLimitAndDays_whenArgsEmpty() {

        when(service.getTopCosts(anyInt(), anyInt())).thenReturn(List.of());
        when(csvExporter.export(anyList())).thenReturn("output/cost-report.csv");

        tool.execute(Map.of());

        verify(service).getTopCosts(7, 10);
    }

    @Test
    void shouldDelegateToServiceFilter_whenServiceArgProvided() {

        when(service.getTopCostsForService(eq("Amazon EC2"), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(csvExporter.export(anyList())).thenReturn("output/cost-report.csv");

        tool.execute(Map.of("service", "Amazon EC2", "days", 7, "limit", 5));

        verify(service).getTopCostsForService("Amazon EC2", 7, 5);
        verify(service, never()).getTopCosts(anyInt(), anyInt());
    }
}
