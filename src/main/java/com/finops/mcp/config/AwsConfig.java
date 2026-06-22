package com.finops.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;

@Configuration
public class AwsConfig {

    // Cost Explorer solo está disponible en us-east-1, independientemente
    // de en qué región operen los recursos reales. DEBE IR HARDCODED.
    @Bean
    public StsClient stsClient() {
        return StsClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }
}