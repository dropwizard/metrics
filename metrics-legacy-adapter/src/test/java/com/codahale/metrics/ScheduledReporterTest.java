package com.codahale.metrics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class ScheduledReporterTest {

    private MetricRegistry metricRegistry = new MetricRegistry();
    private ScheduledReporter scheduledReporter;

    @BeforeEach
    void setUp() throws Exception {
        metricRegistry.register("sw-gauge", (Gauge<Integer>) () -> 28);
        metricRegistry.counter("sw-counter");
        metricRegistry.timer("sw-timer");
        metricRegistry.meter("sw-meter");
        metricRegistry.histogram("sw-histogram");
    }

    @AfterEach
    void tearDown() throws Exception {
        scheduledReporter.stop();
    }

    private ScheduledReporter createScheduledReporter(CountDownLatch latch) {
        return new ScheduledReporter(metricRegistry, "test", MetricFilter.ALL, TimeUnit.MILLISECONDS,
                TimeUnit.MINUTES) {
            @Override
            @SuppressWarnings("rawtypes")
            public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                    SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
                    SortedMap<String, Timer> timers) {
                assertThat(gauges).containsOnlyKeys("sw-gauge");
                assertThat(counters).containsOnlyKeys("sw-counter");
                assertThat(histograms).containsOnlyKeys("sw-histogram");
                assertThat(meters).containsOnlyKeys("sw-meter");
                assertThat(timers).containsOnlyKeys("sw-timer");
                latch.countDown();
            }
        };
    }

    @Test
    void testReport() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        scheduledReporter = createScheduledReporter(latch);
        scheduledReporter.report();

        latch.await(5, TimeUnit.SECONDS);
        assertThat(latch.getCount()).isEqualTo(0);
    }

    @Test
    void testStart() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        scheduledReporter = createScheduledReporter(latch);
        scheduledReporter.start(10, TimeUnit.MILLISECONDS);

        latch.await(5, TimeUnit.SECONDS);
        assertThat(latch.getCount()).isEqualTo(0);
    }

    @Test
    void testStartWithoutDelay() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        scheduledReporter = createScheduledReporter(latch);
        scheduledReporter.start(0, 10, TimeUnit.MILLISECONDS);

        latch.await(5, TimeUnit.SECONDS);
        assertThat(latch.getCount()).isEqualTo(0);
    }
}
