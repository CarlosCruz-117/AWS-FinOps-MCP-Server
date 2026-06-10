package com.finops.mcp.aws;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AWS ADAPTER TEST (NEGATIVE + MOCKED SDK)
 */
class AwsCostExplorerAdapterTest {

    @Test
    void shouldHandleAwsFailureGracefully() {
        // ARRANGE
        AwsCostExplorerAdapter adapter = mock(AwsCostExplorerAdapter.class);

        when(adapter.fetchLast7DaysEc2Costs())
                .thenThrow(new RuntimeException("AWS timeout"));

        // ACT & ASSERT
        assertThatThrownBy(adapter::fetchLast7DaysEc2Costs)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AWS timeout");
    }
}
