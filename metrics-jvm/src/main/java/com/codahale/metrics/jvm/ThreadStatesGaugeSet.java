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

    // do not compute stack traces.
    private final static int STACK_TRACE_DEPTH = 0;

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
        final Map<String, Metric> gauges = new HashMap<>();

        for (final Thread.State state : Thread.State.values()) {
            gauges.put(name(state.toString().toLowerCase(), "count"),
                    (Gauge<Object>) () -> getThreadCount(state));
        }

        gauges.put("count", (Gauge<Integer>) threads::getThreadCount);
        gauges.put("daemon.count", (Gauge<Integer>) threads::getDaemonThreadCount);
        gauges.put("peak.count", (Gauge<Integer>) threads::getPeakThreadCount);
        gauges.put("total_started.count", (Gauge<Long>) threads::getTotalStartedThreadCount);
        gauges.put("deadlock.count", (Gauge<Integer>) () -> deadlockDetector.getDeadlockedThreads().size());
        gauges.put("deadlocks", (Gauge<Set<String>>) deadlockDetector::getDeadlockedThreads);

        return Collections.unmodifiableMap(gauges);
    }

    private int getThreadCount(Thread.State state) {
        final ThreadInfo[] allThreads = getThreadInfo();
        int count = 0;
        for (ThreadInfo info : allThreads) {
            if (info != null && info.getThreadState() == state) {
                count++;
            }
        }
        return count;
    }

    ThreadInfo[] getThreadInfo() {
        return threads.getThreadInfo(threads.getAllThreadIds(), STACK_TRACE_DEPTH);
    }

}
