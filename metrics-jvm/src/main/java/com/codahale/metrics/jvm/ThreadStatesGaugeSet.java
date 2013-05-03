package com.codahale.metrics.jvm;

import static com.codahale.metrics.MetricRegistry.name;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.NoCopyGauge;

/**
 * A set of gauges for the number of threads in their various states and deadlock detection.
 */
public class ThreadStatesGaugeSet implements MetricSet {
    private final ThreadMXBean threads;
    private final ThreadDeadlockDetector deadlockDetector;

    /**
     * Creates a new set of gauges using the default MXBeans.
     */
    public ThreadStatesGaugeSet() {
        this(ManagementFactory.getThreadMXBean(), new ThreadDeadlockDetector());
    }

    /**
     * Creates a new set of gauges using the given MXBean and detector.
     *
     * @param threads          a thread MXBean
     * @param deadlockDetector a deadlock detector
     */
    public ThreadStatesGaugeSet(ThreadMXBean threads,
                                ThreadDeadlockDetector deadlockDetector) {
        this.threads = threads;
        this.deadlockDetector = deadlockDetector;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();

        for (final Thread.State state : Thread.State.values()) {
            gauges.put(name(state.toString().toLowerCase(), "count"),
                       new NoCopyGauge<Object>() {
                          @Override
                           public Object getValue() {
                               return getThreadCount(state);
                           }
                       });
        }

        gauges.put("count", new NoCopyGauge<Integer>() {
            @Override
            public Integer getValue() {
                return threads.getThreadCount();
            }
        });

        gauges.put("daemon.count", new NoCopyGauge<Integer>() {
            @Override
            public Integer getValue() {
                return threads.getDaemonThreadCount();
            }
        });

        gauges.put("deadlocks", new NoCopyGauge<Set<String>>() {
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
