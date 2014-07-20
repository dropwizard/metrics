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
 * Accumulates metrics data and sends it to CloudWatch
 */
public class CloudWatchSendTask {
    private static final String HOST_NAME = getHostName();

    private final String nameSpace;
    private final AmazonCloudWatchClient client;
    private final List<MetricDatum> reqs = new ArrayList<MetricDatum>();

    public CloudWatchSendTask(String nameSpace, AmazonCloudWatchClient client) {
        this.nameSpace = nameSpace;
        this.client = client;
    }

    /**
     * Add metrics to be sent
     * @param metricName
     * @param value
     */
    public void add(String metricName, double value) {
        value = validate(value);

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

    // Amazon CloudWatch rejects values that are either too small or too large.
    // Values must be in the range of 2e-360 to 2e360 (Base 2).
    public static final double MINIMUM_VALUE = Math.pow(2, -360);
    public static final double MAXIMUM_VALUE = Math.pow(2, 360);


    private double validate(double value) {
        if(Double.isInfinite(value) || Double.isNaN(value)){
            throw new IllegalArgumentException("Invalid value :" + value);
        }else{
            // 0 is allowed
            if (value == 0){
                return value;
            }

            // Comply with CloudWatch's restriction on very large/small values
            value = MINIMUM_VALUE > value ? MINIMUM_VALUE : value;
            value = MAXIMUM_VALUE < value ? MAXIMUM_VALUE : value;
            return value;
        }
    }

    public void send() {
        List<MetricDatum> subpackets = new ArrayList<MetricDatum>();
        for (MetricDatum req : reqs) {
            subpackets.add(req);
            // Only 20 data can be sent to AWS at a time
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
