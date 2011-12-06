package com.yammer.metrics.util;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class Utils {
    private static final ThreadPools THREAD_POOLS = new ThreadPools();

    private Utils() { /* unused */ }

    public static Map<String, Map<MetricName, Metric>> sortMetrics(Map<MetricName, Metric> metrics) {
        return sortAndFilterMetrics(metrics, MetricPredicate.ALL);
    }

    public static Map<MetricName, Metric> filterMetrics(Map<MetricName, Metric> metrics, MetricPredicate predicate) {
        final Map<MetricName, Metric> result = new HashMap<MetricName, Metric>();
        for (Entry<MetricName, Metric> entry : metrics.entrySet()) {
            if (predicate.matches(entry.getKey(), entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public static Map<String, Map<MetricName, Metric>> sortAndFilterMetrics(Map<MetricName, Metric> metrics, MetricPredicate predicate) {
        final Map<String, Map<MetricName, Metric>> sortedMetrics = new TreeMap<String, Map<MetricName, Metric>>();
        for (Entry<MetricName, Metric> entry : metrics.entrySet()) {
            final String qualifiedTypeName = entry.getKey().getGroup() + "." + entry.getKey()
                                                                                    .getType();
            if (predicate.matches(entry.getKey(), entry.getValue())) {
                final String scopedName;
                if (entry.getKey().hasScope()) {
                    scopedName = qualifiedTypeName + "." + entry.getKey().getScope();
                } else {
                    scopedName = qualifiedTypeName;
                }
                Map<MetricName, Metric> subMetrics = sortedMetrics.get(scopedName);
                if (subMetrics == null) {
                    subMetrics = new TreeMap<MetricName, Metric>();
                    sortedMetrics.put(scopedName, subMetrics);
                }
                subMetrics.put(entry.getKey(), entry.getValue());
            }
        }
        return sortedMetrics;
    }


    /**
     * Creates a new scheduled thread pool of a given size with the given name.
     *
     * @param poolSize the number of threads to create
     * @param name     the name of the pool
     * @return a new {@link ScheduledExecutorService}
     * @deprecated Get a thread pool via {@link com.yammer.metrics.core.MetricsRegistry#threadPools()}
     *             instead
     */
    public static ScheduledExecutorService newScheduledThreadPool(int poolSize, String name) {
        return THREAD_POOLS.newScheduledThreadPool(poolSize, name);
    }

    /**
     * Shuts down all thread pools created by this class in an orderly fashion.
     *
     * @deprecated Shut down the thread pools object of the relevant {@link
     *             com.yammer.metrics.core.MetricsRegistry} instead
     */
    public static void shutdownThreadPools() {
        THREAD_POOLS.shutdownThreadPools();
    }
}
