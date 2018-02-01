package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class ClockTest {

    @Test
    public void testDefaultClockCanBeUsed() {
        Clock clock = Clock.defaultClock();
        assertThat(clock.getTick()).isGreaterThan(0);
    }

    @Test
    public void testUserTimeClockCanBeUsed() {
        Clock clock = new Clock.UserTimeClock();
        assertThat(clock.getTick()).isGreaterThan(0);
    }

    @Test
    public void testCustomTimeClockCanBeUsed() {
        Clock clock = new Clock() {
            @Override
            public long getTick() {
                return 24;
            }
        };
        assertThat(clock.getTick()).isEqualTo(24);
    }
}
