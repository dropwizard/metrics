package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.MetricsRegistryListener;

import java.util.HashSet;
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

    public Set<MetricsRegistry> getMetricsRegistries(){
        return metricsRegistries;
    }

    public String getName() {
        return name;
    }

    /**
     * This is for backwards compatability, and should not generally be used.
     * @return
     */
    @Deprecated
    public MetricsRegistry getMetricsRegistry(){
        for (MetricsRegistry r : metricsRegistries){
            return r;
        }

        //Should be impossible ot get here.
        return null;
    }
}
