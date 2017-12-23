package com.codahale.metrics;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@RunWith(value = Parameterized.class)
public class MeterApproximationTest {

    @Parameters
    public static Collection<Object[]> ratesPerMinute() {
        Object[][] data = new Object[][]{
                {15}, {60}, {600}, {6000}
        };
        return Arrays.asList(data);
    }

    private final long ratePerMinute;

    public MeterApproximationTest(long ratePerMinute) {
        this.ratePerMinute = ratePerMinute;
    }

    @Test
    public void controlMeter1MinuteMeanApproximation() {
        final Meter meter = simulateMetronome(
                62934, TimeUnit.MILLISECONDS,
                3, TimeUnit.MINUTES);

        assertThat(meter.getOneMinuteRate() * 60.0)
                .isEqualTo(ratePerMinute, offset(0.1 * ratePerMinute));
    }

    @Test
    public void controlMeter5MinuteMeanApproximation() {
        final Meter meter = simulateMetronome(
                62934, TimeUnit.MILLISECONDS,
                13, TimeUnit.MINUTES);

        assertThat(meter.getFiveMinuteRate() * 60.0)
                .isEqualTo(ratePerMinute, offset(0.1 * ratePerMinute));
    }

    @Test
    public void controlMeter15MinuteMeanApproximation() {
        final Meter meter = simulateMetronome(
                62934, TimeUnit.MILLISECONDS,
                38, TimeUnit.MINUTES);

        assertThat(meter.getFifteenMinuteRate() * 60.0)
                .isEqualTo(ratePerMinute, offset(0.1 * ratePerMinute));
    }

    private Meter simulateMetronome(
            long introDelay, TimeUnit introDelayUnit,
            long duration, TimeUnit durationUnit) {

        final ManualClock clock = new ManualClock();
        final Meter meter = new Meter(clock);

        clock.addNanos(introDelayUnit.toNanos(introDelay));

        final long endTick = clock.getTick() + durationUnit.toNanos(duration);
        final long marksIntervalInNanos = TimeUnit.MINUTES.toNanos(1) / ratePerMinute;

        while (clock.getTick() <= endTick) {
            clock.addNanos(marksIntervalInNanos);
            meter.mark();
        }

        return meter;
    }

}
