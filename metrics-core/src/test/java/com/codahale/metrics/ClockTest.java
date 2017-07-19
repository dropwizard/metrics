package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class ClockTest {

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
