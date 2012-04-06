package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;

import java.util.Set;

/**
 * The base class for all metric reporters.
 */
public abstract class AbstractReporter implements Reporter{
    protected final Set<MetricsRegistry> metricsRegistries;
    protected final String name;

    protected AbstractReporter(Set<MetricsRegistry> registries, String name) {
        this.metricsRegistries = registries;
        this.name = name;
    }

    /**
     * Getter for the set of metric registries reported on.
     * @return
     */
    public Set<MetricsRegistry> getMetricsRegistries(){
        return metricsRegistries;
    }

    /**
     * Getter for the {@link Reporter} name.
     * @return
     */
    public String getName() {
        return name;
    }
}
