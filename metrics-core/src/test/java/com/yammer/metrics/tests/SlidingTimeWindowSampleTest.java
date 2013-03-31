package com.yammer.metrics.tests;

import com.yammer.metrics.Clock;
import com.yammer.metrics.SlidingTimeWindowSample;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlidingTimeWindowSampleTest {
    private final Clock clock = mock(Clock.class);
    private final SlidingTimeWindowSample sample = new SlidingTimeWindowSample(10, TimeUnit.NANOSECONDS, clock);

    @Test
    public void storesMeasurementsWithDuplicateTicks() throws Exception {
        when(clock.getTick()).thenReturn(20L);

        sample.update(1);
        sample.update(2);

        assertThat(sample.getSnapshot().getValues())
                .containsOnly(1, 2);
    }

    @Test
    public void boundsMeasurementsToATimeWindow() throws Exception {
        when(clock.getTick()).thenReturn(0L);
        sample.update(1);

        when(clock.getTick()).thenReturn(5L);
        sample.update(2);

        when(clock.getTick()).thenReturn(10L);
        sample.update(3);

        when(clock.getTick()).thenReturn(15L);
        sample.update(4);

        when(clock.getTick()).thenReturn(20L);
        sample.update(5);

        assertThat(sample.getSnapshot().getValues())
                .containsOnly(4, 5);
    }
}
