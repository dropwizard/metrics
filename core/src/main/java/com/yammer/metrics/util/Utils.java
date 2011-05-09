package com.yammer.metrics.util;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class Utils {
    private static final Set<ExecutorService> THREAD_POOLS = new CopyOnWriteArraySet<ExecutorService>();

    private Utils() { /* unused */ }

    public static Map<String, Map<String, Metric>> sortMetrics(Map<MetricName, Metric> metrics) {
        final Map<String, Map<String, Metric>> sortedMetrics =
                new TreeMap<String, Map<String, Metric>>();
        for (Entry<MetricName, Metric> entry : metrics.entrySet()) {
            final String className = entry.getKey().getKlass()
                                          .getCanonicalName()
                                          .replace('$', '.')
                                          .replaceAll("\\.$", "");
            final String scopedName;
            if (entry.getKey().hasScope()) {
                scopedName = className + "." + entry.getKey().getScope();
            } else {
                scopedName = className;
            }
            Map<String, Metric> submetrics = sortedMetrics.get(scopedName);
            if (submetrics == null) {
                submetrics = new TreeMap<String, Metric>();
                sortedMetrics.put(scopedName, submetrics);
            }
            submetrics.put(entry.getKey().getName(), entry.getValue());
        }
        return sortedMetrics;
    }

    /**
     * Creates a new scheduled thread pool of a given size with the given name.
     *
     * @param poolSize the number of threads to create
     * @param name the name of the pool
     * @return a new {@link ScheduledExecutorService}
     */
    public static ScheduledExecutorService newScheduledThreadPool(int poolSize, String name) {
        final ScheduledExecutorService service = Executors.newScheduledThreadPool(poolSize, new NamedThreadFactory(name));
        THREAD_POOLS.add(service);
        return service;
    }

    /**
     * Shuts down all thread pools created by this class in an orderly fashion.
     */
    public static void shutdownThreadPools() {
        for (ExecutorService executor : THREAD_POOLS) {
            executor.shutdown();
        }
    }
}
