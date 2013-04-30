package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * A set of gauges for the number of threads in their various states and deadlock detection.
 */
public class ThreadStatesGaugeSet implements MetricSet {
    private final ThreadMXBean threads;
    private final ThreadDeadlockDetector deadlockDetector;
    private final String metricNamePrefix;

    /**
     * Creates a new set of gauges using the default MXBeans.
     */
    public ThreadStatesGaugeSet() {
        this(null);
    }

    /**
     * Creates a new set of gauges using the default MXBeans.
     * 
     * @param metricNamePrefix prefix for metric names, can be <code>null</code>
     */
    public ThreadStatesGaugeSet(String metricNamePrefix) {
        this(ManagementFactory.getThreadMXBean(), new ThreadDeadlockDetector(), metricNamePrefix);
    }

    /**
     * Creates a new set of gauges using the given MXBean and detector.
     *
     * @param threads          a thread MXBean
     * @param deadlockDetector a deadlock detector
     */
    public ThreadStatesGaugeSet(ThreadMXBean threads,
                                ThreadDeadlockDetector deadlockDetector) {
        this(threads, deadlockDetector, null);
    }
    
    /**
     * Creates a new set of gauges using the given MXBean and detector.
     *
     * @param threads          a thread MXBean
     * @param deadlockDetector a deadlock detector
     * @param metricNamePrefix prefix for metric names, can be <code>null</code>
     */
    public ThreadStatesGaugeSet(ThreadMXBean threads, 
                                ThreadDeadlockDetector deadlockDetector, 
                                String metricNamePrefix) {
        super();
        this.threads = threads;
        this.deadlockDetector = deadlockDetector;
        this.metricNamePrefix = metricNamePrefix;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();

        for (final Thread.State state : Thread.State.values()) {
            gauges.put(name(metricNamePrefix, state.toString().toLowerCase(), "count"),
                       new Gauge<Object>() {
                           @Override
                           public Object getValue() {
                               return getThreadCount(state);
                           }
                       });
        }

        gauges.put(name(metricNamePrefix, "count"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return threads.getThreadCount();
            }
        });

        gauges.put(name(metricNamePrefix, "daemon.count"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return threads.getDaemonThreadCount();
            }
        });

        gauges.put(name(metricNamePrefix, "deadlocks"), new Gauge<Set<String>>() {
            @Override
            public Set<String> getValue() {
                return deadlockDetector.getDeadlockedThreads();
            }
        });

        return Collections.unmodifiableMap(gauges);
    }

    private int getThreadCount(Thread.State state) {
        final ThreadInfo[] allThreads = threads.getThreadInfo(threads.getAllThreadIds());
        int count = 0;
        for (ThreadInfo info : allThreads) {
            if (info != null && info.getThreadState() == state) {
                count++;
            }
        }
        return count;
    }
}
