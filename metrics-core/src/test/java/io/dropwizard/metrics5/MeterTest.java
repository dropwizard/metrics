package io.dropwizard.metrics5;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeterTest {
    private final Clock clock = mock(Clock.class);
    private final Meter meter = new Meter(clock);

    @Before
    public void setUp() throws Exception {
        when(clock.getTick()).thenReturn(0L, TimeUnit.SECONDS.toNanos(10));

    }

    @Test
    public void startsOutWithNoRatesOrCount() {
        assertThat(meter.getCount())
                .isZero();

        assertThat(meter.getSum())
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
    public void marksEventsAndUpdatesRatesAndCount() {
        meter.mark();
        meter.mark(2);

        assertThat(meter.getCount())
                .isEqualTo(3);

        assertThat(meter.getSum())
                .isEqualTo(10000000000L);

        assertThat(meter.getMeanRate())
                .isEqualTo(0.3, offset(0.001));

        assertThat(meter.getOneMinuteRate())
                .isEqualTo(0.1840, offset(0.001));

        assertThat(meter.getFiveMinuteRate())
                .isEqualTo(0.1966, offset(0.001));

        assertThat(meter.getFifteenMinuteRate())
                .isEqualTo(0.1988, offset(0.001));
    }
}
