package com.codahale.metrics;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ScheduledReporterTest {
    private final Gauge<String> gauge = () -> "";
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);

    private final ScheduledExecutorService mockExecutor = mock(ScheduledExecutorService.class);
    private final ScheduledExecutorService customExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService externalExecutor = Executors.newSingleThreadScheduledExecutor();

    private final MetricRegistry registry = new MetricRegistry();
    private final ScheduledReporter reporter = spy(
            new DummyReporter(registry, "example", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)
    );
    private final ScheduledReporter reporterWithNullExecutor = spy(
            new DummyReporter(registry, "example", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, null)
    );
    private final ScheduledReporter reporterWithCustomMockExecutor = new DummyReporter(registry, "example", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, mockExecutor);
    private final ScheduledReporter reporterWithCustomExecutor = new DummyReporter(registry, "example", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, customExecutor);
    private final DummyReporter reporterWithExternallyManagedExecutor = new DummyReporter(registry, "example", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, externalExecutor, false);
    private final ScheduledReporter[] reporters = new ScheduledReporter[] {reporter, reporterWithCustomExecutor, reporterWithExternallyManagedExecutor};

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        registry.register("gauge", gauge);
        registry.register("counter", counter);
        registry.register("histogram", histogram);
        registry.register("meter", meter);
        registry.register("timer", timer);
    }

    @After
    public void tearDown() throws Exception {
        customExecutor.shutdown();
        externalExecutor.shutdown();
        reporter.stop();
        reporterWithNullExecutor.stop();
    }

    @Test
    public void createWithNullMetricRegistry() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        DummyReporter r = null;
        try {
            r = new DummyReporter(null, "example", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, executor);
            Assert.fail("NullPointerException must be thrown !!!");
        } catch (NullPointerException e) {
            Assert.assertEquals("registry == null", e.getMessage());
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    @Test
    public void pollsPeriodically() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        reporter.start(100, 100, TimeUnit.MILLISECONDS, () -> {
            if (latch.getCount() > 0) {
                reporter.report();
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);

        verify(reporter, times(2)).report(
                map("gauge", gauge),
                map("counter", counter),
                map("histogram", histogram),
                map("meter", meter),
                map("timer", timer)
        );
    }

    @Test
    public void shouldUsePeriodAsInitialDelayIfNotSpecifiedOtherwise() throws Exception {
        reporterWithCustomMockExecutor.start(200, TimeUnit.MILLISECONDS);

        verify(mockExecutor, times(1)).scheduleWithFixedDelay(
            any(Runnable.class), eq(200L), eq(200L), eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    public void shouldStartWithSpecifiedInitialDelay() throws Exception {
        reporterWithCustomMockExecutor.start(350, 100, TimeUnit.MILLISECONDS);

        verify(mockExecutor).scheduleWithFixedDelay(
            any(Runnable.class), eq(350L), eq(100L), eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    public void shouldAutoCreateExecutorWhenItNull() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        reporterWithNullExecutor.start(100, 100, TimeUnit.MILLISECONDS, () -> {
            if (latch.getCount() > 0) {
                reporterWithNullExecutor.report();
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
        verify(reporterWithNullExecutor, times(2)).report(
                map("gauge", gauge),
                map("counter", counter),
                map("histogram", histogram),
                map("meter", meter),
                map("timer", timer)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldDisallowToStartReportingMultiple() throws Exception {
        reporter.start(200, TimeUnit.MILLISECONDS);
        reporter.start(200, TimeUnit.MILLISECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldDisallowToStartReportingMultipleTimesOnCustomExecutor() throws Exception {
        reporterWithCustomExecutor.start(200, TimeUnit.MILLISECONDS);
        reporterWithCustomExecutor.start(200, TimeUnit.MILLISECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldDisallowToStartReportingMultipleTimesOnExternallyManagedExecutor() throws Exception {
        reporterWithExternallyManagedExecutor.start(200, TimeUnit.MILLISECONDS);
        reporterWithExternallyManagedExecutor.start(200, TimeUnit.MILLISECONDS);
    }

    @Test
    public void shouldNotFailOnStopIfReporterWasNotStared() {
        for (ScheduledReporter reporter : reporters) {
            reporter.stop();
        }
    }

    @Test
    public void shouldNotFailWhenStoppingMultipleTimes() {
        for (ScheduledReporter reporter : reporters) {
            reporter.start(200, TimeUnit.MILLISECONDS);
            reporter.stop();
            reporter.stop();
            reporter.stop();
        }
    }

    @Test
    public void shouldShutdownExecutorOnStopByDefault() {
        reporterWithCustomExecutor.start(200, TimeUnit.MILLISECONDS);
        reporterWithCustomExecutor.stop();
        assertTrue(customExecutor.isTerminated());
    }

    @Test
    public void shouldNotShutdownExternallyManagedExecutorOnStop() {
        reporterWithExternallyManagedExecutor.start(200, TimeUnit.MILLISECONDS);
        reporterWithExternallyManagedExecutor.stop();
        assertFalse(mockExecutor.isTerminated());
        assertFalse(mockExecutor.isShutdown());
    }

    @Test
    public void shouldCancelScheduledFutureWhenStoppingWithExternallyManagedExecutor() throws InterruptedException, ExecutionException, TimeoutException {
        // configure very frequency rate of execution
        reporterWithExternallyManagedExecutor.start(1, TimeUnit.MILLISECONDS);
        reporterWithExternallyManagedExecutor.stop();
        Thread.sleep(100);

        // executionCount should not increase when scheduled future is canceled properly
        int executionCount = reporterWithExternallyManagedExecutor.executionCount.get();
        Thread.sleep(500);
        assertEquals(executionCount, reporterWithExternallyManagedExecutor.executionCount.get());
    }

    @Test
    public void shouldConvertDurationToMillisecondsPrecisely() {
        assertEquals(2.0E-5, reporter.convertDuration(20), 0.0);
    }

    @Test
    public void shouldReportMetricsOnShutdown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        reporterWithNullExecutor.start(0, 10, TimeUnit.SECONDS, () -> {
            if (latch.getCount() > 0) {
                reporterWithNullExecutor.report();
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
        reporterWithNullExecutor.stop();

        verify(reporterWithNullExecutor, times(2)).report(
                map("gauge", gauge),
                map("counter", counter),
                map("histogram", histogram),
                map("meter", meter),
                map("timer", timer)
        );
    }

    @Test
    public void shouldRescheduleAfterReportFinish() throws Exception {
        // the first report is triggered at T + 0.1 seconds and takes 0.8 seconds
        // after the first report finishes at T + 0.9 seconds the next report is scheduled to run at T + 1.4 seconds
        reporter.start(100, 500, TimeUnit.MILLISECONDS, () -> {
            reporter.report();
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread.sleep(1_000);

        verify(reporter, times(1)).report(
                map("gauge", gauge),
                map("counter", counter),
                map("histogram", histogram),
                map("meter", meter),
                map("timer", timer)
        );
    }

    private <T> SortedMap<String, T> map(String name, T value) {
        final SortedMap<String, T> map = new TreeMap<>();
        map.put(name, value);
        return map;
    }

    private static class DummyReporter extends ScheduledReporter {

        private AtomicInteger executionCount = new AtomicInteger();

        DummyReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
            super(registry, name, filter, rateUnit, durationUnit);
        }

        DummyReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit, ScheduledExecutorService executor) {
            super(registry, name, filter, rateUnit, durationUnit, executor);
        }

        DummyReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit, ScheduledExecutorService executor, boolean shutdownExecutorOnStop) {
            super(registry, name, filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
            executionCount.incrementAndGet();
            // nothing doing!
        }
    }

}
