package com.yammer.metrics.spring;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

import java.util.HashSet;
import java.util.Set;

public class JmxReporterFactory {

    public static JmxReporter createInstance(MetricsRegistry metrics) {
        Set<MetricsRegistry> registries = new HashSet<MetricsRegistry>(1);
        registries.add(metrics);
        return new JmxReporter.Builder(registries, "factoryJMXReporter").build();
    }

}
