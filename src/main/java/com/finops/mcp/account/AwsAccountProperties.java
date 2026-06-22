package com.finops.mcp.account;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "finops")
public record AwsAccountProperties(List<AwsAccountConfig> accounts) {

    public record AwsAccountConfig(
            String accountId,
            String alias,
            String roleArn,
            String region
    ) {
    }
}