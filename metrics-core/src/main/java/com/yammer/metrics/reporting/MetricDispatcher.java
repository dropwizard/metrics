package com.yammer.metrics.reporting;

import com.yammer.metrics.core.*;

public class MetricDispatcher {
    public <T> void dispatch(Metric metric, MetricName name, MetricProcessor<T> processor, T context) throws Exception {
        if (metric instanceof Gauge) {
            processor.processGauge(name, (Gauge<?>) metric, context);
        } else if (metric instanceof Counter) {
            processor.processCounter(name, (Counter) metric, context);
        } else if (metric instanceof Meter) {
            processor.processMeter(name, (Meter) metric, context);
        } else if (metric instanceof Histogram) {
            processor.processHistogram(name, (Histogram) metric, context);
        } else if (metric instanceof Timer) {
            processor.processTimer(name, (Timer) metric, context);
        } else {
            throw new IllegalArgumentException("Unable to dispatch " + metric);
        }
    }
}
