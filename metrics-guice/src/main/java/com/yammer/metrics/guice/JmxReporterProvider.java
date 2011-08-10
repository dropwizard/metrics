package com.yammer.metrics.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.yammer.metrics.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

public class JmxReporterProvider implements Provider<JmxReporter>
{
    private final MetricsRegistry metricsRegistry;

    @Inject
    public JmxReporterProvider(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    @Override
    public JmxReporter get() {
        JmxReporter reporter = new JmxReporter(metricsRegistry);
        reporter.start();
        return reporter;
    }
}
