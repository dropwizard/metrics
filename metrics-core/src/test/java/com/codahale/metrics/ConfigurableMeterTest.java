package com.codahale.metrics;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.offset;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurableMeterTest
{
    private final Clock clock = mock(Clock.class);
    private final ConfigurableMeter meter = new ConfigurableMeter(clock, TimeUnit.SECONDS, new long[] {30, 60, 120});

    @Before
    public void setUp() throws Exception {
        when(clock.getTick()).thenReturn(0L, TimeUnit.SECONDS.toNanos(10));
    }

    @Test
    public void getRateWithGoodInterval() throws ConfigurableMeter.InvalidInterval {
        meter.getRate(30, TimeUnit.SECONDS);
        meter.getRate(60, TimeUnit.SECONDS);
        meter.getRate(1, TimeUnit.MINUTES);
    }

    @Test(expected = ConfigurableMeter.InvalidInterval.class)
    public void getRateWithBadInterval() throws ConfigurableMeter.InvalidInterval {
        meter.getRate(13, TimeUnit.SECONDS);
    }

    @Test
    public void marksEventsAndUpdatesRatesAndCount() throws Exception {
        meter.mark();
        meter.mark(2);

        assertThat(meter.getMeanRate())
                .isEqualTo(0.3, offset(0.001));

        assertThat(meter.getRate(1, TimeUnit.MINUTES))
                .isEqualTo(0.1840, offset(0.001));
    }
}
