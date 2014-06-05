package com.codahale.metrics.graphite;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * User: wendel.schultz
 * Date: 11/6/13
 */
public class CompositeReportingMetricRegistry extends MetricRegistry {
    private final MetricRegistry[] metricRegistries;

    public CompositeReportingMetricRegistry(final MetricRegistry... metricRegistries) {
        if(metricRegistries == null || metricRegistries.length == 0) {
            throw new IllegalArgumentException("Must provide metric registries");
        }
        this.metricRegistries = metricRegistries;
    }


    /**
     * Returns a map of all the gauges in the registry and their names which match the given filter.
     *
     * @param filter the metric filter to match
     * @return all the gauges in the registry
     */
    @Override
    public SortedMap<String, Gauge> getGauges(final MetricFilter filter) {

        final SortedMap<String, Gauge> metrics = new TreeMap<String, Gauge>();

        for(MetricRegistry registry: metricRegistries){
            metrics.putAll(registry.getGauges(filter));
        }

        return Collections.unmodifiableSortedMap(metrics);
    }

    /**
     * Returns a map of all the counters in the registry and their names which match the given
     * filter.
     *
     * @param filter the metric filter to match
     * @return all the counters in the registry
     */
    @Override
    public SortedMap<String, Counter> getCounters(final MetricFilter filter) {
        final SortedMap<String, Counter> metrics = new TreeMap<String, Counter>();

        for(MetricRegistry registry: metricRegistries){
            metrics.putAll(registry.getCounters(filter));
        }

        return Collections.unmodifiableSortedMap(metrics);
    }

    /**
     * Returns a map of all the histograms in the registry and their names which match the given
     * filter.
     *
     * @param filter the metric filter to match
     * @return all the histograms in the registry
     */
    @Override
    public SortedMap<String, Histogram> getHistograms(final MetricFilter filter) {
        final SortedMap<String, Histogram> metrics = new TreeMap<String, Histogram>();

        for(MetricRegistry registry: metricRegistries){
            metrics.putAll(registry.getHistograms(filter));
        }

        return Collections.unmodifiableSortedMap(metrics);
    }

    /**
     * Returns a map of all the meters in the registry and their names which match the given filter.
     *
     * @param filter the metric filter to match
     * @return all the meters in the registry
     */
    @Override
    public SortedMap<String, Meter> getMeters(final MetricFilter filter) {
        final SortedMap<String, Meter> metrics = new TreeMap<String, Meter>();

        for(MetricRegistry registry: metricRegistries){
            metrics.putAll(registry.getMeters(filter));
        }

        return Collections.unmodifiableSortedMap(metrics);
    }

    /**
     * Returns a map of all the timers in the registry and their names which match the given filter.
     *
     * @param filter the metric filter to match
     * @return all the timers in the registry
     */
    @Override
    public SortedMap<String, Timer> getTimers(final MetricFilter filter) {
        final SortedMap<String, Timer> metrics = new TreeMap<String, Timer>();

        for(MetricRegistry registry: metricRegistries){
            metrics.putAll(registry.getTimers(filter));
        }

        return Collections.unmodifiableSortedMap(metrics);
    }

    public int getMeasuredMetricsCount(){
        int count = 0;
        for(MetricRegistry registry: metricRegistries){
            count += registry.getMetrics().size();
        }

        return count;
    }
}
