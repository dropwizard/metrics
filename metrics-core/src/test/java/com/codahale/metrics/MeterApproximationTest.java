package com.codahale.metrics;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class MeterApproximationTest {

    public static LongStream ratesPerMinute() {
        return LongStream.of(15, 60, 600, 6000);
    }

    @ParameterizedTest
    @MethodSource("ratesPerMinute")
    public void controlMeter1MinuteMeanApproximation(long ratePerMinute) {
        final Meter meter = simulateMetronome(ratePerMinute,
                62934, TimeUnit.MILLISECONDS,
                3, TimeUnit.MINUTES);

        assertThat(meter.getOneMinuteRate() * 60.0)
                .isEqualTo(ratePerMinute, offset(0.1 * ratePerMinute));
    }

    @ParameterizedTest
    @MethodSource("ratesPerMinute")
    public void controlMeter5MinuteMeanApproximation(long ratePerMinute) {
        final Meter meter = simulateMetronome(ratePerMinute,
                62934, TimeUnit.MILLISECONDS,
                13, TimeUnit.MINUTES);

        assertThat(meter.getFiveMinuteRate() * 60.0)
                .isEqualTo(ratePerMinute, offset(0.1 * ratePerMinute));
    }

    @ParameterizedTest
    @MethodSource("ratesPerMinute")
    public void controlMeter15MinuteMeanApproximation(long ratePerMinute) {
        final Meter meter = simulateMetronome(ratePerMinute,
                62934, TimeUnit.MILLISECONDS,
                38, TimeUnit.MINUTES);

        assertThat(meter.getFifteenMinuteRate() * 60.0)
                .isEqualTo(ratePerMinute, offset(0.1 * ratePerMinute));
    }

    private Meter simulateMetronome(long ratePerMinute,
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
