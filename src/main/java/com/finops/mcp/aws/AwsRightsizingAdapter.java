package com.finops.mcp.aws;

import com.finops.mcp.account.AwsAccountProperties.AwsAccountConfig;
import com.finops.mcp.account.AwsClientFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.GetRightsizingRecommendationRequest;
import software.amazon.awssdk.services.costexplorer.model.RecommendationTarget;
import software.amazon.awssdk.services.costexplorer.model.RightsizingRecommendation;
import software.amazon.awssdk.services.costexplorer.model.RightsizingRecommendationConfiguration;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class AwsRightsizingAdapter {

    private final AwsClientFactory clientFactory;

    public AwsRightsizingAdapter(AwsClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /**
     * Recomendaciones oficiales de AWS Cost Explorer para la cuenta dada.
     * Puede devolver lista vacía si la cuenta no tiene histórico suficiente
     * o Rightsizing no está habilitado.
     */
    public List<RightsizingRecommendation> fetchAwsRightsizingRecommendations(
            AwsAccountConfig account) {

        try (CostExplorerClient client = clientFactory.costExplorerClient(account)) {

            GetRightsizingRecommendationRequest request =
                    GetRightsizingRecommendationRequest.builder()
                            .service("AmazonEC2")
                            .configuration(RightsizingRecommendationConfiguration.builder()
                                    .recommendationTarget(RecommendationTarget.SAME_INSTANCE_FAMILY)
                                    .benefitsConsidered(false)
                                    .build())
                            .build();

            return client.getRightsizingRecommendation(request).rightsizingRecommendations();
        }
    }

    /**
     * Lista instancias EC2 en ejecución para la cuenta y región dadas.
     */
    public List<Instance> fetchRunningInstances(AwsAccountConfig account) {

        try (Ec2Client client = clientFactory.ec2Client(account)) {

            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .filters(Filter.builder()
                            .name("instance-state-name")
                            .values("running")
                            .build())
                    .build();

            List<Instance> instances = new ArrayList<>();
            client.describeInstances(request).reservations()
                    .forEach(r -> instances.addAll(r.instances()));
            return instances;
        }
    }

    /**
     * CPU media de una instancia en los últimos {@code days} días.
     * Devuelve -1 si no hay datos suficientes.
     */
    public double fetchAverageCpuUtilization(AwsAccountConfig account,
                                             String instanceId,
                                             int days) {

        try (CloudWatchClient client = clientFactory.cloudWatchClient(account)) {

            Instant end = Instant.now();
            Instant start = end.minus(days, ChronoUnit.DAYS);

            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/EC2")
                    .metricName("CPUUtilization")
                    .dimensions(Dimension.builder()
                            .name("InstanceId")
                            .value(instanceId)
                            .build())
                    .startTime(start)
                    .endTime(end)
                    .period(86400)
                    .statistics(Statistic.AVERAGE)
                    .build();

            return client.getMetricStatistics(request).datapoints().stream()
                    .mapToDouble(Datapoint::average)
                    .average()
                    .orElse(-1.0);
        }
    }
}