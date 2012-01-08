package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Clock;
import org.junit.Test;

import java.lang.management.ManagementFactory;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ClockTest {
    @Test
    public void cpuTimeClock() throws Exception {
        final Clock.CpuTimeClock clock = new Clock.CpuTimeClock();

        assertThat((double) clock.time(),
                   is(closeTo(System.currentTimeMillis(), 100)));

        assertThat((double) clock.tick(),
                   is(closeTo(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime(), 1000000)));
    }

    @Test
    public void userTimeClock() throws Exception {
        final Clock.UserTimeClock clock = new Clock.UserTimeClock();

        assertThat((double) clock.time(),
                   is(closeTo(System.currentTimeMillis(), 100)));

        assertThat((double) clock.tick(),
                   is(closeTo(System.nanoTime(), 100000)));
    }

    @Test
    public void defaultsToUserTime() throws Exception {
        assertThat(Clock.defaultClock(),
                   is(instanceOf(Clock.UserTimeClock.class)));
    }
}
