package com.codahale.metrics;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedScheduledExecutorServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentedScheduledExecutorServiceTest.class);

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedScheduledExecutorService instrumentedScheduledExecutor = new InstrumentedScheduledExecutorService(scheduledExecutor, registry, "xs");

    private final Meter submitted = registry.meter("xs.submitted");

    private final Counter running = registry.counter("xs.running");
    private final Meter completed = registry.meter("xs.completed");
    private final Timer duration = registry.timer("xs.duration");

    private final Meter scheduledOnce = registry.meter("xs.scheduled.once");
    private final Meter scheduledRepetitively = registry.meter("xs.scheduled.repetitively");
    private final Counter scheduledOverrun = registry.counter("xs.scheduled.overrun");
    private final Histogram percentOfPeriod = registry.histogram("xs.scheduled.percent-of-period");

    @Test
    public void testSubmitRunnable() throws Exception {
        assertThat(submitted.getCount()).isZero();

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isZero();
        assertThat(duration.getCount()).isZero();

        assertThat(scheduledOnce.getCount()).isZero();
        assertThat(scheduledRepetitively.getCount()).isZero();
        assertThat(scheduledOverrun.getCount()).isZero();
        assertThat(percentOfPeriod.getCount()).isZero();

        Future<?> theFuture = instrumentedScheduledExecutor.submit(() -> {
            assertThat(submitted.getCount()).isEqualTo(1);

            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isZero();
            assertThat(duration.getCount()).isZero();

            assertThat(scheduledOnce.getCount()).isZero();
            assertThat(scheduledRepetitively.getCount()).isZero();
            assertThat(scheduledOverrun.getCount()).isZero();
            assertThat(percentOfPeriod.getCount()).isZero();
        });

        theFuture.get();

        assertThat(submitted.getCount()).isEqualTo(1);

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);

        assertThat(scheduledOnce.getCount()).isZero();
        assertThat(scheduledRepetitively.getCount()).isZero();
        assertThat(scheduledOverrun.getCount()).isZero();
        assertThat(percentOfPeriod.getCount()).isZero();
    }

    @Test
    public void testScheduleRunnable() throws Exception {
        assertThat(submitted.getCount()).isZero();

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isZero();
        assertThat(duration.getCount()).isZero();

        assertThat(scheduledOnce.getCount()).isZero();
        assertThat(scheduledRepetitively.getCount()).isZero();
        assertThat(scheduledOverrun.getCount()).isZero();
        assertThat(percentOfPeriod.getCount()).isZero();

        ScheduledFuture<?> theFuture = instrumentedScheduledExecutor.schedule(() -> {
            assertThat(submitted.getCount()).isZero();

            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isZero();
            assertThat(duration.getCount()).isZero();

            assertThat(scheduledOnce.getCount()).isEqualTo(1);
            assertThat(scheduledRepetitively.getCount()).isZero();
            assertThat(scheduledOverrun.getCount()).isZero();
            assertThat(percentOfPeriod.getCount()).isZero();
        }, 10L, TimeUnit.MILLISECONDS);

        theFuture.get();

        assertThat(submitted.getCount()).isZero();

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);

        assertThat(scheduledOnce.getCount()).isEqualTo(1);
        assertThat(scheduledRepetitively.getCount()).isZero();
        assertThat(scheduledOverrun.getCount()).isZero();
        assertThat(percentOfPeriod.getCount()).isZero();
    }

    @Test
    public void testSubmitCallable() throws Exception {
        assertThat(submitted.getCount()).isZero();

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isZero();
        assertThat(duration.getCount()).isZero();

        assertThat(scheduledOnce.getCount()).isZero();
        assertThat(scheduledRepetitively.getCount()).isZero();
        assertThat(scheduledOverrun.getCount()).isZero();
        assertThat(percentOfPeriod.getCount()).isZero();

        final Object obj = new Object();

        Future<Object> theFuture = instrumentedScheduledExecutor.submit(() -> {
            assertThat(submitted.getCount()).isEqualTo(1);

            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isZero();
            assertThat(duration.getCount()).isZero();

            assertThat(scheduledOnce.getCount()).isZero();
            assertThat(scheduledRepetitively.getCount()).isZero();
            assertThat(scheduledOverrun.getCount()).isZero();
            assertThat(percentOfPeriod.getCount()).isZero();

            return obj;
        });

        assertThat(theFuture.get()).isEqualTo(obj);

        assertThat(submitted.getCount()).isEqualTo(1);

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);

        assertThat(scheduledOnce.getCount()).isZero();
        assertThat(scheduledRepetitively.getCount()).isZero();
        assertThat(scheduledOverrun.getCount()).isZero();
        assertThat(percentOfPeriod.getCount()).isZero();
    }

    @Test
    public void testScheduleCallable() throws Exception {
        assertThat(submitted.getCount()).isZero();

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isZero();
        assertThat(duration.getCount()).isZero();

        assertThat(scheduledOnce.getCount()).isZero();
        assertThat(scheduledRepetitively.getCount()).isZero();
        assertThat(scheduledOverrun.getCount()).isZero();
        assertThat(percentOfPeriod.getCount()).isZero();

        final Object obj = new Object();

        ScheduledFuture<Object> theFuture = instrumentedScheduledExecutor.schedule(() -> {
            assertThat(submitted.getCount()).isZero();

            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isZero();
            assertThat(duration.getCount()).isZero();

            assertThat(scheduledOnce.getCount()).isEqualTo(1);
            assertThat(scheduledRepetitively.getCount()).isZero();
            assertThat(scheduledOverrun.getCount()).isZero();
            assertThat(percentOfPeriod.getCount()).isZero();

            return obj;
        }, 10L, TimeUnit.MILLISECONDS);

        assertThat(theFuture.get()).isEqualTo(obj);

        assertThat(submitted.getCount()).isZero();

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);

        assertThat(scheduledOnce.getCount()).isEqualTo(1);
        assertThat(scheduledRepetitively.getCount()).isZero();
        assertThat(scheduledOverrun.getCount()).isZero();
        assertThat(percentOfPeriod.getCount()).isZero();
    }

    @Test
    public void testScheduleFixedRateCallable() throws Exception {
        assertThat(submitted.getCount()).isZero();

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isZero();
        assertThat(duration.getCount()).isZero();

        assertThat(scheduledOnce.getCount()).isZero();
        assertThat(scheduledRepetitively.getCount()).isZero();
        assertThat(scheduledOverrun.getCount()).isZero();
        assertThat(percentOfPeriod.getCount()).isZero();

        ScheduledFuture<?> theFuture = instrumentedScheduledExecutor.scheduleAtFixedRate(() -> {
            assertThat(submitted.getCount()).isZero();

            assertThat(running.getCount()).isEqualTo(1);

            assertThat(scheduledOnce.getCount()).isEqualTo(0);
            assertThat(scheduledRepetitively.getCount()).isEqualTo(1);

            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }, 10L, 10L, TimeUnit.MILLISECONDS);

        TimeUnit.MILLISECONDS.sleep(100);
        theFuture.cancel(true);
        TimeUnit.MILLISECONDS.sleep(100);

        assertThat(submitted.getCount()).isZero();

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isNotEqualTo(0);
        assertThat(duration.getCount()).isNotEqualTo(0);
        assertThat(duration.getSnapshot().size()).isNotEqualTo(0);

        assertThat(scheduledOnce.getCount()).isZero();
        assertThat(scheduledRepetitively.getCount()).isEqualTo(1);
        assertThat(scheduledOverrun.getCount()).isNotEqualTo(0);
        assertThat(percentOfPeriod.getCount()).isNotEqualTo(0);
    }

    @Test
    public void testScheduleFixedDelayCallable() throws Exception {
        assertThat(submitted.getCount()).isZero();

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isZero();
        assertThat(duration.getCount()).isZero();

        assertThat(scheduledOnce.getCount()).isZero();
        assertThat(scheduledRepetitively.getCount()).isZero();
        assertThat(scheduledOverrun.getCount()).isZero();
        assertThat(percentOfPeriod.getCount()).isZero();

        ScheduledFuture<?> theFuture = instrumentedScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                assertThat(submitted.getCount()).isZero();

                assertThat(running.getCount()).isEqualTo(1);

                assertThat(scheduledOnce.getCount()).isEqualTo(0);
                assertThat(scheduledRepetitively.getCount()).isEqualTo(1);

                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                return;
            }
        }, 10L, 10L, TimeUnit.MILLISECONDS);

        TimeUnit.MILLISECONDS.sleep(100);
        theFuture.cancel(true);
        TimeUnit.MILLISECONDS.sleep(100);

        assertThat(submitted.getCount()).isZero();

        assertThat(running.getCount()).isZero();
        assertThat(completed.getCount()).isNotEqualTo(0);
        assertThat(duration.getCount()).isNotEqualTo(0);
        assertThat(duration.getSnapshot().size()).isNotEqualTo(0);
    }

    @After
    public void tearDown() throws Exception {
        instrumentedScheduledExecutor.shutdown();
        if (!instrumentedScheduledExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
            LOGGER.error("InstrumentedScheduledExecutorService did not terminate.");
        }
    }

}
