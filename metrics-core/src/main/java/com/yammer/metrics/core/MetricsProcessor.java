package com.yammer.metrics.core;

public interface MetricsProcessor<T> {

    public void processMeter(MetricName name, Metered meter, T context) throws Exception;

    public void processCounter(MetricName name, Counter counter, T context) throws Exception;

    public void processHistogram(MetricName name, Histogram histogram, T context) throws Exception;

    public void processTimer(MetricName name, Timer timer, T context) throws Exception;

    public void processGauge(MetricName name, Gauge<?> gauge, T context) throws Exception;

}
