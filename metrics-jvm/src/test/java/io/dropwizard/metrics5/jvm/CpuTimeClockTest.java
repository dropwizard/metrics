package io.dropwizard.metrics5.jvm;

import org.junit.Test;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class CpuTimeClockTest {

    @Test
    public void cpuTimeClock() {
        final CpuTimeClock clock = new CpuTimeClock();

        final long clockTime = clock.getTime();
        final long systemTime = System.currentTimeMillis();
        assertThat((double) clockTime).isEqualTo(systemTime, offset(200.0));

        final long clockTick = clock.getTick();
        final long systemTick = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        assertThat((double) clockTick).isEqualTo(systemTick, offset(1000000.0));
    }
}