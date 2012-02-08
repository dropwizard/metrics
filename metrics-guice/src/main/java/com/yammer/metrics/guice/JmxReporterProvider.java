package com.yammer.metrics.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

public class JmxReporterProvider implements Provider<JmxReporter> {
    private final MetricsRegistry metricsRegistry;
    private final HealthCheckRegistry checkRegistry;

    @Inject
    public JmxReporterProvider(MetricsRegistry metricsRegistry, HealthCheckRegistry checkRegistry) {
        this.metricsRegistry = metricsRegistry;
        this.checkRegistry = checkRegistry;
    }

    @Override
    public JmxReporter get() {
        final JmxReporter reporter = new JmxReporter(metricsRegistry, checkRegistry);
        reporter.start();
        return reporter;
    }
}
