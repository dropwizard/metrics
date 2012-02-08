package com.yammer.metrics.spring;

import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

public class JmxReporterFactory {

    public static JmxReporter createInstance(MetricsRegistry metrics, HealthCheckRegistry checks) {
        final JmxReporter reporter = new JmxReporter(metrics, checks);
        reporter.start();
        return reporter;
    }

}
