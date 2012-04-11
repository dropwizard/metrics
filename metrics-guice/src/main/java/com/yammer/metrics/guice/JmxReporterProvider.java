package com.yammer.metrics.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

@Singleton
public class JmxReporterProvider implements Provider<JmxReporter> {
    private final MetricsRegistry metricsRegistry;

    @Inject
    public JmxReporterProvider(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    @Override
    public JmxReporter get() {
        if (metricsRegistry == Metrics.defaultRegistry()) {
            return JmxReporter.getDefault();
        }

        final JmxReporter reporter = new JmxReporter(metricsRegistry);
        reporter.start();
        return reporter;
    }
}
