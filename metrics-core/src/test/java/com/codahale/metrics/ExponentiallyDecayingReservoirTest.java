package com.codahale.metrics;

import com.codahale.metrics.Timer.Context;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ExponentiallyDecayingReservoirTest {
    @Test
    public void aReservoirOf100OutOf1000Elements() {
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(100, 0.99);
        for (int i = 0; i < 1000; i++) {
            reservoir.update(i);
        }

        assertThat(reservoir.size())
                .isEqualTo(100);

        final Snapshot snapshot = reservoir.getSnapshot();

        assertThat(snapshot.size())
                .isEqualTo(100);

        assertAllValuesBetween(reservoir, 0, 1000);
    }

    @Test
    public void aReservoirOf100OutOf10Elements() {
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(100, 0.99);
        for (int i = 0; i < 10; i++) {
            reservoir.update(i);
        }

        final Snapshot snapshot = reservoir.getSnapshot();

        assertThat(snapshot.size())
                .isEqualTo(10);

        assertThat(snapshot.size())
                .isEqualTo(10);

        assertAllValuesBetween(reservoir, 0, 10);
    }

    @Test
    public void aHeavilyBiasedReservoirOf100OutOf1000Elements() {
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(1000, 0.01);
        for (int i = 0; i < 100; i++) {
            reservoir.update(i);
        }


        assertThat(reservoir.size())
                .isEqualTo(100);

        final Snapshot snapshot = reservoir.getSnapshot();

        assertThat(snapshot.size())
                .isEqualTo(100);

        assertAllValuesBetween(reservoir, 0, 100);
    }

    @Test
    public void longPeriodsOfInactivityShouldNotCorruptSamplingState() {
        final ManualClock clock = new ManualClock();
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(10, 0.15, clock);

        // add 1000 values at a rate of 10 values/second
        for (int i = 0; i < 1000; i++) {
            reservoir.update(1000 + i);
            clock.addMillis(100);
        }
        assertThat(reservoir.getSnapshot().size())
                .isEqualTo(10);
        assertAllValuesBetween(reservoir, 1000, 2000);

        // wait for 15 hours and add another value.
        // this should trigger a rescale. Note that the number of samples will be reduced to 1
        // because scaling factor equal to zero will remove all existing entries after rescale.
        clock.addHours(15);
        reservoir.update(2000);
        assertThat(reservoir.getSnapshot().size())
                .isEqualTo(1);
        assertAllValuesBetween(reservoir, 1000, 2001);


        // add 1000 values at a rate of 10 values/second
        for (int i = 0; i < 1000; i++) {
            reservoir.update(3000 + i);
            clock.addMillis(100);
        }
        assertThat(reservoir.getSnapshot().size())
                .isEqualTo(10);
        assertAllValuesBetween(reservoir, 3000, 4000);
    }

    @Test
    public void longPeriodsOfInactivity_fetchShouldResample() {
        final ManualClock clock = new ManualClock();
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(10,
                0.015,
                clock);

        // add 1000 values at a rate of 10 values/second
        for (int i = 0; i < 1000; i++) {
            reservoir.update(1000 + i);
            clock.addMillis(100);
        }
        assertThat(reservoir.getSnapshot().size())
                .isEqualTo(10);
        assertAllValuesBetween(reservoir, 1000, 2000);

        // wait for 20 hours and take snapshot.
        // this should trigger a rescale. Note that the number of samples will be reduced to 0
        // because scaling factor equal to zero will remove all existing entries after rescale.
        clock.addHours(20);
        Snapshot snapshot = reservoir.getSnapshot();
        assertThat(snapshot.getMax()).isEqualTo(0);
        assertThat(snapshot.getMean()).isEqualTo(0);
        assertThat(snapshot.getMedian()).isEqualTo(0);
        assertThat(snapshot.size()).isEqualTo(0);
    }

    @Test
    public void emptyReservoirSnapshot_shouldReturnZeroForAllValues() {
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(100, 0.015,
                new ManualClock());

        Snapshot snapshot = reservoir.getSnapshot();
        assertThat(snapshot.getMax()).isEqualTo(0);
        assertThat(snapshot.getMean()).isEqualTo(0);
        assertThat(snapshot.getMedian()).isEqualTo(0);
        assertThat(snapshot.size()).isEqualTo(0);
    }

    @Test
    public void removeZeroWeightsInSamplesToPreventNaNInMeanValues() {
        final ManualClock clock = new ManualClock();
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(1028, 0.015, clock);
        Timer timer = new Timer(reservoir, clock);

        Context context = timer.time();
        clock.addMillis(100);
        context.stop();

        for (int i = 1; i < 48; i++) {
            clock.addHours(1);
            assertThat(reservoir.getSnapshot().getMean()).isBetween(0.0, Double.MAX_VALUE);
        }
    }

    @Test
    public void multipleUpdatesAfterlongPeriodsOfInactivityShouldNotCorruptSamplingState() throws Exception {
        // This test illustrates the potential race condition in rescale that
        // can lead to a corrupt state.  Note that while this test uses updates
        // exclusively to trigger the race condition, two concurrent updates
        // may be made much more likely to trigger this behavior if executed
        // while another thread is constructing a snapshot of the reservoir;
        // that thread then holds the read lock when the two competing updates
        // are executed and the race condition's window is substantially
        // expanded.

        // Run the test several times.
        for (int attempt = 0; attempt < 10; attempt++) {
            final ManualClock clock = new ManualClock();
            final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(10,
                    0.015,
                    clock);

            // Various atomics used to communicate between this thread and the
            // thread created below.
            final AtomicBoolean running = new AtomicBoolean(true);
            final AtomicInteger threadUpdates = new AtomicInteger(0);
            final AtomicInteger testUpdates = new AtomicInteger(0);

            final Thread thread = new Thread(() -> {
                int previous = 0;
                while (running.get()) {
                    // Wait for the test thread to update it's counter
                    // before updaing the reservoir.
                    while (true) {
                        int next = testUpdates.get();
                        if (previous < next) {
                            previous = next;
                            break;
                        }
                    }

                    // Update the reservoir.  This needs to occur at the
                    // same time as the test thread's update.
                    reservoir.update(1000);

                    // Signal the main thread; allows the next update
                    // attempt to begin.
                    threadUpdates.incrementAndGet();
                }
            });

            thread.start();

            int sum = 0;
            int previous = -1;
            for (int i = 0; i < 100; i++) {
                // Wait for 15 hours before attempting the next concurrent
                // update.  The delay here needs to be sufficiently long to
                // overflow if an update attempt is allowed to add a value to
                // the reservoir without rescaling.  Note that:
                // e(alpha*(15*60*60)) =~ 10^351 >> Double.MAX_VALUE =~ 1.8*10^308.
                clock.addHours(15);

                // Signal the other thread; asynchronously updates the reservoir.
                testUpdates.incrementAndGet();

                // Delay a variable length of time.  Without a delay here this
                // thread is heavily favored and the race condition is almost
                // never observed.
                for (int j = 0; j < i; j++)
                    sum += j;

                // Competing reservoir update.
                reservoir.update(1000);

                // Wait for the other thread to finish it's update.
                while (true) {
                    int next = threadUpdates.get();
                    if (previous < next) {
                        previous = next;
                        break;
                    }
                }
            }

            // Terminate the thread.
            running.set(false);
            testUpdates.incrementAndGet();
            thread.join();

            // Test failures will result in normWeights that are not finite;
            // checking the mean value here is sufficient.
            assertThat(reservoir.getSnapshot().getMean()).isBetween(0.0, Double.MAX_VALUE);

            // Check the value of sum; should prevent the JVM from optimizing
            // out the delay loop entirely.
            assertThat(sum).isEqualTo(161700);
        }
    }

    @Test
    public void spotLift() {
        final ManualClock clock = new ManualClock();
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(1000,
                0.015,
                clock);

        final int valuesRatePerMinute = 10;
        final int valuesIntervalMillis = (int) (TimeUnit.MINUTES.toMillis(1) / valuesRatePerMinute);
        // mode 1: steady regime for 120 minutes
        for (int i = 0; i < 120 * valuesRatePerMinute; i++) {
            reservoir.update(177);
            clock.addMillis(valuesIntervalMillis);
        }

        // switching to mode 2: 10 minutes more with the same rate, but larger value
        for (int i = 0; i < 10 * valuesRatePerMinute; i++) {
            reservoir.update(9999);
            clock.addMillis(valuesIntervalMillis);
        }

        // expect that quantiles should be more about mode 2 after 10 minutes
        assertThat(reservoir.getSnapshot().getMedian())
                .isEqualTo(9999);
    }

    @Test
    public void spotFall() {
        final ManualClock clock = new ManualClock();
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(1000,
                0.015,
                clock);

        final int valuesRatePerMinute = 10;
        final int valuesIntervalMillis = (int) (TimeUnit.MINUTES.toMillis(1) / valuesRatePerMinute);
        // mode 1: steady regime for 120 minutes
        for (int i = 0; i < 120 * valuesRatePerMinute; i++) {
            reservoir.update(9998);
            clock.addMillis(valuesIntervalMillis);
        }

        // switching to mode 2: 10 minutes more with the same rate, but smaller value
        for (int i = 0; i < 10 * valuesRatePerMinute; i++) {
            reservoir.update(178);
            clock.addMillis(valuesIntervalMillis);
        }

        // expect that quantiles should be more about mode 2 after 10 minutes
        assertThat(reservoir.getSnapshot().get95thPercentile())
                .isEqualTo(178);
    }

    @Test
    public void quantiliesShouldBeBasedOnWeights() {
        final ManualClock clock = new ManualClock();
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(1000,
                0.015,
                clock);
        for (int i = 0; i < 40; i++) {
            reservoir.update(177);
        }

        clock.addSeconds(120);

        for (int i = 0; i < 10; i++) {
            reservoir.update(9999);
        }

        assertThat(reservoir.getSnapshot().size())
                .isEqualTo(50);

        // the first added 40 items (177) have weights 1 
        // the next added 10 items (9999) have weights ~6 
        // so, it's 40 vs 60 distribution, not 40 vs 10
        assertThat(reservoir.getSnapshot().getMedian())
                .isEqualTo(9999);
        assertThat(reservoir.getSnapshot().get75thPercentile())
                .isEqualTo(9999);
    }

    private static void assertAllValuesBetween(ExponentiallyDecayingReservoir reservoir,
                                               double min,
                                               double max) {
        for (double i : reservoir.getSnapshot().getValues()) {
            assertThat(i)
                    .isLessThan(max)
                    .isGreaterThanOrEqualTo(min);
        }
    }

}
