package com.yammer.metrics.core;

public interface MetricsProcessor<T> {

    public void processMeter(MetricName name, Metered meter, T context) throws Exception;

    public void processCounter(MetricName name, CounterMetric counter, T context) throws Exception;

    public void processHistogram(MetricName name, HistogramMetric histogram, T context) throws Exception;

    public void processTimer(MetricName name, TimerMetric timer, T context) throws Exception;

    public void processGauge(MetricName name, GaugeMetric<?> gauge, T context) throws Exception;

}
