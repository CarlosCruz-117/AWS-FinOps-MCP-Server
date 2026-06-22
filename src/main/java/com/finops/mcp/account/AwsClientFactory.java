package com.finops.mcp.account;

import com.finops.mcp.account.AwsAccountProperties.AwsAccountConfig;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

/**
 * Construye clientes por cuenta
 */
@Component
public class AwsClientFactory {

    private final StsClient stsClient;

    public AwsClientFactory(StsClient stsClient) {
        this.stsClient = stsClient;
    }

    private AwsCredentialsProvider credentialsFor(AwsAccountConfig account) {

        if (account.roleArn() == null || account.roleArn().isBlank()) {
            return DefaultCredentialsProvider.create();
        }

        AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                .roleArn(account.roleArn())
                .roleSessionName("finops-mcp-" + account.alias())
                .build();

        return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(stsClient)
                .refreshRequest(assumeRoleRequest)
                .build();
    }

    /**
     * Cost Explorer SOLO está disponible en us-east-1, independientemente
     * de la región operativa de la cuenta.
     */
    public CostExplorerClient costExplorerClient(AwsAccountConfig account) {
        return CostExplorerClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsFor(account))
                .build();
    }

    public Ec2Client ec2Client(AwsAccountConfig account) {
        return Ec2Client.builder()
                .region(Region.of(account.region()))
                .credentialsProvider(credentialsFor(account))
                .build();
    }

    public CloudWatchClient cloudWatchClient(AwsAccountConfig account) {
        return CloudWatchClient.builder()
                .region(Region.of(account.region()))
                .credentialsProvider(credentialsFor(account))
                .build();
    }
}