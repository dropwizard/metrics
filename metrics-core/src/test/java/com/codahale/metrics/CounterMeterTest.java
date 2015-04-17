package com.codahale.metrics;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CounterMeterTest {
    private final Clock clock = mock(Clock.class);
    private final CounterMeter meter = new CounterMeter(clock);

    @Before
    public void setUp() throws Exception {
        when(clock.getTick()).thenReturn(0L, TimeUnit.SECONDS.toNanos(10));

    }

    @Test
    public void startsOutWithNoRatesOrCount() throws Exception {
        assertThat(meter.getCount())
                .isZero();

        assertThat(meter.getMeanRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(meter.getOneMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(meter.getFiveMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(meter.getFifteenMinuteRate())
                .isEqualTo(0.0, offset(0.001));
    }

    @Test
    public void marksEventsAndUpdatesRatesAndCount() throws Exception {
        meter.mark();
        meter.mark(2);

        assertThat(meter.getMeanRate())
                .isEqualTo(0.3, offset(0.001));

        assertThat(meter.getOneMinuteRate())
                .isEqualTo(0.1840, offset(0.001));

        assertThat(meter.getFiveMinuteRate())
                .isEqualTo(0.1966, offset(0.001));

        assertThat(meter.getFifteenMinuteRate())
                .isEqualTo(0.1988, offset(0.001));
    }

    @Test
    public void startsAtZero() throws Exception {
        assertThat(meter.getCount())
                .isZero();
    }

    @Test
    public void incrementsByOne() throws Exception {
    	meter.inc();

        assertThat(meter.getCount())
                .isEqualTo(1);
    }

    @Test
    public void incrementsByAnArbitraryDelta() throws Exception {
    	meter.inc(12);

        assertThat(meter.getCount())
                .isEqualTo(12);
    }

    @Test
    public void decrementsByOne() throws Exception {
    	meter.dec();

        assertThat(meter.getCount())
                .isEqualTo(-1);
    }

    @Test
    public void decrementsByAnArbitraryDelta() throws Exception {
    	meter.dec(12);

        assertThat(meter.getCount())
                .isEqualTo(-12);
    }
}
