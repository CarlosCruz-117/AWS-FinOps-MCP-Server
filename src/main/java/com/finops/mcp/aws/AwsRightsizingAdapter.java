package com.finops.mcp.aws;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class AwsRightsizingAdapter {

    private final CostExplorerClient costExplorerClient;
    private final Ec2Client ec2Client;
    private final CloudWatchClient cloudWatchClient;

    public AwsRightsizingAdapter(
            CostExplorerClient costExplorerClient,
            Ec2Client ec2Client,
            CloudWatchClient cloudWatchClient
    ) {
        this.costExplorerClient = costExplorerClient;
        this.ec2Client = ec2Client;
        this.cloudWatchClient = cloudWatchClient;
    }

    /**
     * Recomendaciones oficiales de AWS Cost Explorer.
     * Puede devolver lista vacía si la cuenta no tiene histórico suficiente
     * o Rightsizing no está habilitado.
     */
    public List<RightsizingRecommendation> fetchAwsRightsizingRecommendations() {

        GetRightsizingRecommendationRequest request = GetRightsizingRecommendationRequest.builder()
                .service("AmazonEC2")
                .configuration(RightsizingRecommendationConfiguration.builder()
                        .recommendationTarget(RecommendationTarget.SAME_INSTANCE_FAMILY)
                        .benefitsConsidered(false)
                        .build())
                .build();

        GetRightsizingRecommendationResponse response =
                costExplorerClient.getRightsizingRecommendation(request);

        return response.rightsizingRecommendations();
    }

    /**
     * Lista instancias EC2 en ejecución, con su tipo actual.
     */
    public List<Instance> fetchRunningInstances() {

        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .filters(Filter.builder()
                        .name("instance-state-name")
                        .values("running")
                        .build())
                .build();

        List<Instance> instances = new ArrayList<>();

        for (Reservation reservation : ec2Client.describeInstances(request).reservations()) {
            instances.addAll(reservation.instances());
        }

        return instances;
    }

    /**
     * CPU media de una instancia en los últimos {@code days} días.
     * Devuelve -1 si no hay datos suficientes.
     */
    public double fetchAverageCpuUtilization(String instanceId, int days) {

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
                .period(86400) // 1 día
                .statistics(Statistic.AVERAGE)
                .build();

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

        return response.datapoints().stream()
                .mapToDouble(Datapoint::average)
                .average()
                .orElse(-1.0);
    }
}