package com.yammer.metrics.core;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * An abstraction for how time passes. It is passed to {@link TimerMetric} to track timing.
 */
public abstract class Clock {

    /**
     * Gets the current time tick
     *
     * @return time tick in nanoseconds
     */
    public abstract long tick();

    /**
     * Gets the current time in milliseconds
     *
     * @return time in milliseconds
     */
    public long time() {
        return System.currentTimeMillis();
    }

    /**
     * The default clock to use.
     */
    public static final Clock DEFAULT = new UserTime();

    /**
     * Default implementation, uses {@link System#nanoTime()}.
     */
    public static class UserTime extends Clock {
        @Override
        public long tick() {
            return System.nanoTime();
        }
    }

    /**
     * Another implementation, uses {@link ThreadMXBean#getCurrentThreadCpuTime()}
     */
    public static class CpuTime extends Clock {
        private static final ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();

        @Override
        public long tick() {
            return threadMxBean.getCurrentThreadCpuTime();
        }
    }
}
