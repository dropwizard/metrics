package com.yammer.metrics.core;

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
    public abstract long tick();

    /**
     * Returns the current time in milliseconds.
     *
     * @return time in milliseconds
     */
    public long time() {
        return System.currentTimeMillis();
    }

    private static final Clock DEFAULT = new UserTimeClock();

    /**
     * The default clock to use.
     *
     * @return the default {@link Clock} instance
     *
     * @see com.yammer.metrics.core.Clock.UserTimeClock
     */
    public static Clock defaultClock() {
        return DEFAULT;
    }


    /**
     * A clock implementation which returns the current time in epoch nanoseconds.
     */
    public static class UserTimeClock extends Clock {
        @Override
        public long tick() {
            return System.nanoTime();
        }
    }

    /**
     * A clock implementation which returns the current thread's CPU time.
     */
    public static class CpuTimeClock extends Clock {
        private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

        @Override
        public long tick() {
            return THREAD_MX_BEAN.getCurrentThreadCpuTime();
        }
    }
}
