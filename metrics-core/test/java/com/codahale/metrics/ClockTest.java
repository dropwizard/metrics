package com.codahale.metrics;

import org.junit.Test;

import java.lang.management.ManagementFactory;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.data.Offset.offset;

public class ClockTest {
    @Test
    public void cpuTimeClock() throws Exception {
        final Clock.CpuTimeClock clock = new Clock.CpuTimeClock();

        assertThat((double) clock.getTime())
                .isEqualTo(System.currentTimeMillis(),
                           offset(100.0));

        assertThat((double) clock.getTick())
                   .isEqualTo(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime(),
                              offset(1000000.0));
    }

    @Test
    public void userTimeClock() throws Exception {
        final Clock.UserTimeClock clock = new Clock.UserTimeClock();

        assertThat((double) clock.getTime())
                .isEqualTo(System.currentTimeMillis(),
                           offset(100.0));

        assertThat((double) clock.getTick())
                .isEqualTo(System.nanoTime(),
                           offset(100000.0));
    }

    @Test
    public void defaultsToUserTime() throws Exception {
        assertThat(Clock.defaultClock())
                .isInstanceOf(Clock.UserTimeClock.class);
    }
}
