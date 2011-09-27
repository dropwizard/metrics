package com.yammer.metrics.reporting;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.AbstractReporter;

public class MockReporter extends AbstractReporter {
    public MockReporter(MetricsRegistry metricsRegistry, String name) {
        super(metricsRegistry, "mock-reporter");
    }
    
    public Map<MetricName, Metric> getAllMetrics() {
        return metricsRegistry.allMetrics();
    }
    
    public boolean hasMetric(String name) {
        return this.getAllMetrics().containsKey(name);
    }
    
    public Metric getMetric(String name) {
        return this.getAllMetrics().get(name);
    }

    @Override
    public void run() { }

    @Override
    public void start(long period, TimeUnit unit) { }
}
