package com.codahale.metrics.cloudwatch;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * A client to AWS CloudWatch
 */
public class CloudWatchPacket {
    private static final String HOST_NAME = getHostName();

    private final String nameSpace;
    private final AmazonCloudWatchClient client;
    private final List<MetricDatum> reqs = new ArrayList<MetricDatum>();

    public CloudWatchPacket(String nameSpace, AmazonCloudWatchClient client) {
        this.nameSpace = nameSpace;
        this.client = client;
    }

    public void add(String metricName, double value) {
        MetricDatum datum =
                new MetricDatum()
                        .withMetricName(metricName)
                        .withUnit(StandardUnit.None)
                        .withValue(value)
                        .withDimensions(
                                new Dimension()
                                        .withName("hostname")
                                        .withValue(HOST_NAME));
        reqs.add(datum);
    }

    public void send() {
        List<MetricDatum> subpackets = new ArrayList<MetricDatum>();
        for (MetricDatum req : reqs) {
            subpackets.add(req);
            if (subpackets.size() == 20) {
                client.putMetricData(new PutMetricDataRequest().withNamespace(nameSpace).withMetricData(subpackets));
                subpackets = new ArrayList<MetricDatum>();
            }
        }


        if (!subpackets.isEmpty()) {
            client.putMetricData(new PutMetricDataRequest().withNamespace(nameSpace).withMetricData(subpackets));
        }
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown_host";
        }
    }

}
