package com.yammer.metrics.core;

import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

/**
 * An abstraction for how time passes. It is passed to {@link TimerMetric} to
 * track timing.
 */
public interface Clock {
    /**
     * Gets the current time tick
     *
     * @return time tick in nanoseconds
     */
    long tick();

    /**
     * The default clock to use.
     */
    public static final Clock DEFAULT = new UserTime();

    /**
     * Default implementation, uses {@link System#nanoTime()}.
     */
    public static class UserTime implements Clock {
        @Override
        public long tick() {
            return System.nanoTime();
        }
    }

    /**
     * Another implementation, uses {@link ThreadMXBean#getCurrentThreadCpuTime()}
     */
    public static class CpuTime implements Clock {
        private static ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();

        @Override
        public long tick() {
            return threadMxBean.getCurrentThreadCpuTime();
        }
    }
}
