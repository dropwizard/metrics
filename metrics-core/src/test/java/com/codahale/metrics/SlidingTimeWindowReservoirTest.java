package com.codahale.metrics;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlidingTimeWindowReservoirTest {
    @Test
    public void storesMeasurementsWithDuplicateTicks() {
        final Clock clock = mock(Clock.class);
        final SlidingTimeWindowReservoir reservoir = new SlidingTimeWindowReservoir(10, NANOSECONDS, clock);

        when(clock.getTick()).thenReturn(20L);

        reservoir.update(1);
        reservoir.update(2);

        assertThat(reservoir.getSnapshot().getValues())
                .containsOnly(1, 2);
    }

    @Test
    public void boundsMeasurementsToATimeWindow() {
        final Clock clock = mock(Clock.class);
        final SlidingTimeWindowReservoir reservoir = new SlidingTimeWindowReservoir(10, NANOSECONDS, clock);

        when(clock.getTick()).thenReturn(0L);
        reservoir.update(1);

        when(clock.getTick()).thenReturn(5L);
        reservoir.update(2);

        when(clock.getTick()).thenReturn(10L);
        reservoir.update(3);

        when(clock.getTick()).thenReturn(15L);
        reservoir.update(4);

        when(clock.getTick()).thenReturn(20L);
        reservoir.update(5);

        assertThat(reservoir.getSnapshot().getValues())
                .containsOnly(4, 5);
    }

    @Test
    public void testGetTickOverflow() {
        final Random random = new Random(0);
        final int window = 128;

        // Note: 'threshold' defines the number of updates submitted to the reservoir after overflowing
        for (int threshold : Arrays.asList(0, 1, 2, 127, 128, 129, 255, 256, 257)) {

            // Note: 'updatePerTick' defines the number of updates submitted to the reservoir between each tick
            for (int updatesPerTick : Arrays.asList(1, 2, 127, 128, 129, 255, 256, 257)) {
                //logger.info("Executing test: threshold={}, updatesPerTick={}", threshold, updatesPerTick);

                // Set the clock to overflow in (2*window+1)ns
                final ManualClock clock = new ManualClock();
                clock.addNanos(Long.MAX_VALUE / 256 - 2 * window - clock.getTick());
                assertThat(clock.getTick() * 256).isGreaterThan(0);

                // Create the reservoir
                final SlidingTimeWindowReservoir reservoir = new SlidingTimeWindowReservoir(window, NANOSECONDS, clock);

                int updatesAfterThreshold = 0;
                while (true) {
                    // Update the reservoir
                    for (int i = 0; i < updatesPerTick; i++)
                        reservoir.update(0);

                    // Randomly check the reservoir size
                    if (random.nextDouble() < 0.1) {
                        assertThat(reservoir.size())
                                .as("Bad reservoir size with: threshold=%d, updatesPerTick=%d", threshold, updatesPerTick)
                                .isLessThanOrEqualTo(window * 256);
                    }

                    // Update the clock
                    clock.addNanos(1);

                    // If the clock has overflowed start counting updates
                    if ((clock.getTick() * 256) < 0) {
                        if (updatesAfterThreshold++ >= threshold)
                            break;
                    }
                }

                // Check the final reservoir size
                assertThat(reservoir.size())
                        .as("Bad final reservoir size with: threshold=%d, updatesPerTick=%d", threshold, updatesPerTick)
                        .isLessThanOrEqualTo(window * 256);

                // Advance the clock far enough to clear the reservoir.  Note that here the window only loosely defines
                // the reservoir window; when updatesPerTick is greater than 128 the sliding window will always be well
                // ahead of the current clock time, and advances in getTick while in trim (called randomly above from
                // size and every 256 updates).  Until the clock "catches up" advancing the clock will have no effect on
                // the reservoir, and reservoir.size() will merely move the window forward 1/256th of a ns - as such, an
                // arbitrary increment of 1s here was used instead to advance the clock well beyond any updates recorded
                // above.
                clock.addSeconds(1);

                // The reservoir should now be empty
                assertThat(reservoir.size())
                        .as("Bad reservoir size after delay with: threshold=%d, updatesPerTick=%d", threshold, updatesPerTick)
                        .isEqualTo(0);
            }
        }
    }
}