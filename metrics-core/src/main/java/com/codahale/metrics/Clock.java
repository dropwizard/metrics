package com.codahale.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * An abstraction for how time passes. It is passed to {@link Timer} to track timing.
 */
public abstract class Clock {
    /**
     * Returns the current time tick.
     *
     * @return time tick in nanoseconds
     */
    public abstract long getTick();

    /**
     * Returns the current time in milliseconds.
     *
     * @return time in milliseconds
     */
    public long getTime() {
        return System.currentTimeMillis();
    }

    private static final Clock DEFAULT = new UserTimeClock();

    /**
     * The default clock to use.
     *
     * @return the default {@link Clock} instance
     *
     * @see Clock.UserTimeClock
     */
    public static Clock defaultClock() {
        return DEFAULT;
    }

    /**
     * A clock implementation which returns the elapsed time in nanoseconds.
     */
    public static class UserTimeClock extends Clock {
        // System.nanoTime can be arbitrary, so offset from a starting point
        // so the values are not very large (or negative.) All we want is
        // elapsed time.
        private static final long OFFSET = System.nanoTime();

        @Override
        public long getTick() {
            return System.nanoTime() - OFFSET;
        }
    }

    /**
     * A clock implementation which returns the current thread's CPU time.
     */
    public static class CpuTimeClock extends Clock {
        private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

        @Override
        public long getTick() {
            return THREAD_MX_BEAN.getCurrentThreadCpuTime();
        }
    }
}
