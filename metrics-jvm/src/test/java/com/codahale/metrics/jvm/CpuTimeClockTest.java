package com.codahale.metrics.jvm;

import org.junit.Test;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class CpuTimeClockTest {

    @Test
    public void cpuTimeClock() {
        final CpuTimeClock clock = new CpuTimeClock();

        assertThat((double) clock.getTime())
                .isEqualTo(System.currentTimeMillis(),
                        offset(200.0));

        assertThat((double) clock.getTick())
                .isEqualTo(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime(),
                        offset(1000000.0));
    }
}