package com.yammer.metrics.librato;

import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;

import java.util.concurrent.TimeUnit;

/**
 * User: mihasya
 * Date: 6/17/12
 * Time: 10:57 PM
 * a LibratoBatch that understand Metrics-specific types
 */
public class MetricsLibratoBatch extends LibratoBatch {
    public MetricsLibratoBatch(int postBatchSize, long timeout, TimeUnit timeoutUnit) {
        super(postBatchSize, timeout, timeoutUnit);
    }

    public void addGauge(String name, Gauge gauge) {
        addGaugeMeasurement(name, (Number) gauge.getValue());
    }

    public void addSummarizable(String name, Summarizable summarizable) {
        // TODO: I bet sum_squares can also be calculated, but I'm too fucking tired for wikipedia right now
        addMeasurement(new MultiSampleGaugeMeasurement(name, summarizable.getMax(), summarizable.getMin(), summarizable.getSum() / summarizable.getMean(), summarizable.getSum(), null));
    }

    public void addSampling(String name, Sampling sampling) {
        Snapshot snapshot = sampling.getSnapshot();
        addMeasurement(new SingleValueGaugeMeasurement(name+".median", snapshot.getMedian()));
        addMeasurement(new SingleValueGaugeMeasurement(name+".75th", snapshot.get75thPercentile()));
        addMeasurement(new SingleValueGaugeMeasurement(name+".95th", snapshot.get95thPercentile()));
        addMeasurement(new SingleValueGaugeMeasurement(name+".98th", snapshot.get98thPercentile()));
        addMeasurement(new SingleValueGaugeMeasurement(name+".99th", snapshot.get99thPercentile()));
        addMeasurement(new SingleValueGaugeMeasurement(name+".999th", snapshot.get999thPercentile()));
    }

    public void addMetered(String name, Metered meter) {
        addMeasurement(new SingleValueGaugeMeasurement(name+".count", meter.getCount()));
        addMeasurement(new SingleValueGaugeMeasurement(name+".meanRate", meter.getMeanRate()));
        addMeasurement(new SingleValueGaugeMeasurement(name+".1MinuteRate", meter.getOneMinuteRate()));
        addMeasurement(new SingleValueGaugeMeasurement(name+".5MinuteRate", meter.getFiveMinuteRate()));
        addMeasurement(new SingleValueGaugeMeasurement(name+".15MinuteRate", meter.getFifteenMinuteRate()));
    }
}
