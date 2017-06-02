package com.codahale.metrics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ScheduledReporterTest {
    private final Gauge gauge = mock(Gauge.class);
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);
    private final ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
    private final ScheduledExecutorService executor = mock(ScheduledExecutorService.class);

    private final MetricRegistry registry = new MetricRegistry();
    private final ScheduledReporter reporter = spy(
            new DummyReporter(registry, "example", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)
    );
    private final ScheduledReporter reporterWithNullExecutor = spy(
            new DummyReporter(registry, "example", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, null)
    );
    private final ScheduledReporter reporterWithCustomExecutor = new DummyReporter(registry, "example", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, executor);
    private final ScheduledReporter reporterWithExternallyManagedExecutor = new DummyReporter(registry, "example", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, executor, false);
    private final ScheduledReporter[] reporters = new ScheduledReporter[] {reporter, reporterWithCustomExecutor, reporterWithExternallyManagedExecutor};

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        registry.register("gauge", gauge);
        registry.register("counter", counter);
        registry.register("histogram", histogram);
        registry.register("meter", meter);
        registry.register("timer", timer);

        when(executor.scheduleAtFixedRate(any(Runnable.class), any(Long.class), any(Long.class), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(scheduledFuture);
    }

    @After
    public void tearDown() throws Exception {
        reporter.stop();
        reporterWithNullExecutor.stop();
    }

    @Test
    public void pollsPeriodically() throws Exception {
        reporter.start(200, TimeUnit.MILLISECONDS);

        Thread.sleep(500);
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
        reporterWithCustomExecutor.start(200, TimeUnit.MILLISECONDS);

        verify(executor, times(1)).scheduleAtFixedRate(
            any(Runnable.class), eq(200L), eq(200L), eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    public void shouldStartWithSpecifiedInitialDelay() throws Exception {
        reporterWithCustomExecutor.start(350, 100, TimeUnit.MILLISECONDS);

        verify(executor).scheduleAtFixedRate(
            any(Runnable.class), eq(350L), eq(100L), eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    public void shouldAutoCreateExecutorWhenItNull() throws Exception {
        reporterWithNullExecutor.start(200, TimeUnit.MILLISECONDS);

        Thread.sleep(500);
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
        verify(executor).shutdown();
        verify(executor).shutdownNow();
    }

    @Test
    public void shouldNotShutdownExternallyManagedExecutorOnStop() {
        reporterWithExternallyManagedExecutor.start(200, TimeUnit.MILLISECONDS);
        reporterWithExternallyManagedExecutor.stop();
        verify(executor, never()).shutdown();
        verify(executor, never()).shutdownNow();
    }

    @Test
    public void shouldCancelScheduledFutureWhenStoppingWithExternallyManagedExecutor() throws InterruptedException, ExecutionException, TimeoutException {
        reporterWithExternallyManagedExecutor.start(200, TimeUnit.MILLISECONDS);
        reporterWithExternallyManagedExecutor.stop();
        // should cancel future
        verify(scheduledFuture).cancel(false);
        // should wait 1 second to future complete
        verify(scheduledFuture).get(1, TimeUnit.SECONDS);
    }

    @Test
    public void shouldIgnoreExecutionExceptionByDesign() throws InterruptedException, ExecutionException, TimeoutException {
        when(scheduledFuture.get(1, TimeUnit.SECONDS)).thenThrow(ExecutionException.class);
        reporterWithExternallyManagedExecutor.start(200, TimeUnit.MILLISECONDS);
        reporterWithExternallyManagedExecutor.stop(); // TimeoutException should be ignored
    }

    @Test
    public void shouldRestoreInterruptionFlagWhenStoppingWithExternallyManagedExecutor() throws InterruptedException, ExecutionException, TimeoutException {
        when(scheduledFuture.get(1, TimeUnit.SECONDS)).thenThrow(new InterruptedException());
        reporterWithExternallyManagedExecutor.start(200, TimeUnit.MILLISECONDS);
        reporterWithExternallyManagedExecutor.stop();
        assertTrue(Thread.interrupted());
    }

    @Test
    public void shouldIgnoreTimeoutExceptionWhenFutureIsNotCancelledInOneSecond() throws InterruptedException, ExecutionException, TimeoutException {
        when(scheduledFuture.get(1, TimeUnit.SECONDS)).thenThrow(new TimeoutException());
        reporterWithExternallyManagedExecutor.start(200, TimeUnit.MILLISECONDS);
        reporterWithExternallyManagedExecutor.stop(); // TimeoutException should be ignored
    }

    @Test
    public void shouldConvertDurationToMillisecondsPrecisely() {
        assertEquals(2.0E-5, reporter.convertDuration(20), 0.0);
    }

    private <T> SortedMap<String, T> map(String name, T value) {
        final SortedMap<String, T> map = new TreeMap<String, T>();
        map.put(name, value);
        return map;
    }

    private static class DummyReporter extends ScheduledReporter {

        public DummyReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
            super(registry, name, filter, rateUnit, durationUnit);
        }

        public DummyReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit, ScheduledExecutorService executor) {
            super(registry, name, filter, rateUnit, durationUnit, executor);
        }

        public DummyReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit, ScheduledExecutorService executor, boolean shutdownExecutorOnStop) {
            super(registry, name, filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
            // nothing doing!
        }
    }

}
