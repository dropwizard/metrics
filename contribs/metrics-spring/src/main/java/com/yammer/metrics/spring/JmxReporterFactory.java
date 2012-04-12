package com.yammer.metrics.spring;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

public class JmxReporterFactory {

    public static JmxReporter createInstance(MetricsRegistry metrics) {
        final JmxReporter reporter = new JmxReporter(metrics);
        reporter.start();
        return reporter;
    }

}
