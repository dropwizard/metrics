package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExponentialMovingAveragesTest {
    @Test
    public void testMaxTicks() {
        final Clock clock = mock(Clock.class);
        when(clock.getTick()).thenReturn(0L, Long.MAX_VALUE);
        final ExponentialMovingAverages ema = new ExponentialMovingAverages(clock);
        ema.update(Long.MAX_VALUE);
        ema.tickIfNecessary();
        final long secondNanos = TimeUnit.SECONDS.toNanos(1);
        assertEquals(ema.getM1Rate(), Double.MIN_NORMAL * secondNanos, 0.0);
        assertEquals(ema.getM5Rate(), Double.MIN_NORMAL * secondNanos, 0.0);
        assertEquals(ema.getM15Rate(), Double.MIN_NORMAL * secondNanos, 0.0);
    }
}
