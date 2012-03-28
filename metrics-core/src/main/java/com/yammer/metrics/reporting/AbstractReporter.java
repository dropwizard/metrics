package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.MetricsRegistryListener;

import java.util.HashSet;
import java.util.Set;

/**
 * The base class for all metric reporters.
 */
public abstract class AbstractReporter implements Reporter{
    //TODO Should probably make this a map =(
    protected final Set<MetricsRegistry> metricsRegistries;

    /**
     * Creates a new {@link AbstractReporter} instance.
     *
     * @param registry the {@link MetricsRegistry} containing the metrics this reporter will
     *                 report
     */
    protected AbstractReporter(MetricsRegistry registry) {
        metricsRegistries = new HashSet<MetricsRegistry>(1);
        metricsRegistries.add(registry);
    }

    protected AbstractReporter(Set<MetricsRegistry> registries) {
        this.metricsRegistries = registries;
    }

    public Set<MetricsRegistry> getMetricsRegistries(){
        return metricsRegistries;
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
