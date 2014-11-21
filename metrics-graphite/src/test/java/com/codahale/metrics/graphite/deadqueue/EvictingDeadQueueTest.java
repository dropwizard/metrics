package com.codahale.metrics.graphite.deadqueue;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.net.UnknownHostException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EvictingDeadQueueTest {

    private final long timestamp = 1000198;
    private final Clock clock = mock(Clock.class);
    private final Graphite graphite = mock(Graphite.class);
    private final MetricRegistry registry = mock(MetricRegistry.class);

    private final GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
            .withClock(clock)
            .prefixedWith("prefix")
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .deadQueue(new EvictingDeadQueue(10))
            .build(graphite);

    @Before
    public void setUp() throws Exception {
        when(clock.getTime()).thenReturn(timestamp * 1000);
    }

    @Test
    public void flushDeadQueueAfterReconnect() throws Exception {
        doThrow(new UnknownHostException("UNKNOWN-HOST")).doNothing().when(graphite).connect();

        reportGauge(1);
        reportGauge(2);

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).send("prefix.gauge", "2", timestamp);
        inOrder.verify(graphite).send("prefix.gauge", "1", timestamp);
    }

    @Test
    public void removeElementIfOverMaxSize() throws Exception {
        doThrow(new UnknownHostException("UNKNOWN-HOST")).when(graphite).connect();
        DeadQueue deadQueue = new EvictingDeadQueue(1);
        GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
                .withClock(clock)
                .prefixedWith("prefix")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .deadQueue(deadQueue)
                .build(graphite);


        reportGauge(reporter, 1);
        reportGauge(reporter, 2);
        Entry entry = deadQueue.poll();

        assertThat(entry.getValue()).isEqualTo("2");
        assertThat(deadQueue.isEmpty()).isTrue() ;
    }

    private void reportGauge(int value) {
        reportGauge(this.reporter, value);
    }

    private void reportGauge(GraphiteReporter reporter, int value) {
        reporter.report(map("gauge", gauge(value)),
                new TreeMap<String, Counter>(),
                new TreeMap<String, Histogram>(),
                new TreeMap<String, Meter>(),
                new TreeMap<String, Timer>());
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<String, T>();
        map.put(name, metric);
        return map;
    }

    private <T> Gauge gauge(T value) {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(value);
        return gauge;
    }
}