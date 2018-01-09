package io.dropwizard.metrics5.jvm;

import io.dropwizard.metrics5.Clock;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * A clock implementation which returns the current thread's CPU time.
 */
public class CpuTimeClock extends Clock {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

    @Override
    public long getTick() {
        return THREAD_MX_BEAN.getCurrentThreadCpuTime();
    }
}
