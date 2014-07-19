package com.codahale.metrics.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.CachedGauge;

/**
 * A variation of ThreadStatesGaugeSet that caches the ThreadInfo[] objects for
 * a given interval.
 */
public class CachedThreadStatesGaugeSet extends ThreadStatesGaugeSet {

    private final CachedGauge<ThreadInfo[]> threadInfo;

    /**
     * Creates a new set of gauges using the given MXBean and detector.
     * Caches the information for the given interval and time unit.
     *
     * @param threadMXBean     a thread MXBean
     * @param deadlockDetector a deadlock detector
     * @param interval         cache interval
     * @param unit             cache interval time unit
     */
    public CachedThreadStatesGaugeSet(final ThreadMXBean threadMXBean, ThreadDeadlockDetector deadlockDetector,
                                      long interval, TimeUnit unit) {
        super(threadMXBean, deadlockDetector);
        threadInfo = new CachedGauge<ThreadInfo[]>(interval, unit) {
            @Override
           protected ThreadInfo[] loadValue() {
                return CachedThreadStatesGaugeSet.super.getThreadInfo();
            }
        };
    }

    /**
     * Creates a new set of gauges using the default MXBeans.
     * Caches the information for the given interval and time unit.
     * @param interval         cache interval
     * @param unit             cache interval time unit
     */
    public CachedThreadStatesGaugeSet(long interval, TimeUnit unit) {
        this(ManagementFactory.getThreadMXBean(), new ThreadDeadlockDetector(), interval, unit);
    }

    @Override
    ThreadInfo[] getThreadInfo() {
        return threadInfo.getValue();
    }

}
