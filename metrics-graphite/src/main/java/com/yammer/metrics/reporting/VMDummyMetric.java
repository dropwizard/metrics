package com.yammer.metrics.reporting;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsProcessor;

/**
 * This is a dummy metric that only exists so that a metric reporter can be registered for vm metrics.
 */
public class VMDummyMetric implements Metric {

    @Override
    public <T> void processWith(MetricsProcessor<T> reporter, MetricName name, T context) throws Exception {
        //do noting
    }

}
