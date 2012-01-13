package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class MetricsRegistryTest {
    private MetricsRegistry registry;

    @Before
    public void setUp() throws Exception {
        this.registry = new MetricsRegistry();
    }

    @After
    public void tearDown() throws Exception {
        registry.shutdown();
    }

    @Test
    public void sortingMetricNamesSortsThemByClassThenScopeThenName() throws Exception {
        final MetricName one = new MetricName(Object.class, "one");
        final MetricName two = new MetricName(Object.class, "two");
        final MetricName three = new MetricName(String.class, "three");

        final Counter mOne = registry.newCounter(Object.class, "one");
        final Counter mTwo = registry.newCounter(Object.class, "two");
        final Counter mThree = registry.newCounter(String.class, "three");

        final SortedMap<String, SortedMap<MetricName, Metric>> sortedMetrics = new TreeMap<String, SortedMap<MetricName, Metric>>();
        final TreeMap<MetricName, Metric> objectMetrics = new TreeMap<MetricName, Metric>();
        objectMetrics.put(one, mOne);
        objectMetrics.put(two, mTwo);
        sortedMetrics.put(Object.class.getCanonicalName(), objectMetrics);

        final TreeMap<MetricName, Metric> stringMetrics = new TreeMap<MetricName, Metric>();
        stringMetrics.put(three, mThree);
        sortedMetrics.put(String.class.getCanonicalName(), stringMetrics);

        assertThat(registry.groupedMetrics(),
                   is(sortedMetrics));
    }

    @Test
    public void listenersRegisterNewMetrics() throws Exception {
        final MetricsRegistryListener listener = mock(MetricsRegistryListener.class);
        registry.addListener(listener);

        final Gauge<?> gauge = mock(Gauge.class);
        registry.newGauge(MetricsRegistryTest.class, "gauge", gauge);
        final Counter counter = registry.newCounter(MetricsRegistryTest.class, "counter");
        final Histogram histogram = registry.newHistogram(MetricsRegistryTest.class, "histogram");
        final Meter meter = registry.newMeter(MetricsRegistryTest.class,
                                              "meter",
                                              "things",
                                              TimeUnit.SECONDS);
        final Timer timer = registry.newTimer(MetricsRegistryTest.class, "timer");

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "gauge"), gauge);

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "counter"), counter);

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "histogram"), histogram);

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "meter"), meter);

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "timer"), timer);
    }

    @Test
    public void removedListenersDoNotReceiveEvents() throws Exception {
        final MetricsRegistryListener listener = mock(MetricsRegistryListener.class);
        registry.addListener(listener);

        final Counter counter1 = registry.newCounter(MetricsRegistryTest.class, "counter1");

        registry.removeListener(listener);

        final Counter counter2 = registry.newCounter(MetricsRegistryTest.class, "counter2");

        verify(listener).onMetricAdded(new MetricName(MetricsRegistryTest.class, "counter1"), counter1);

        verify(listener, never()).onMetricAdded(new MetricName(MetricsRegistryTest.class, "counter2"), counter2);
    }

    @Test
    public void metricsCanBeRemoved() throws Exception {
        final MetricsRegistryListener listener = mock(MetricsRegistryListener.class);
        registry.addListener(listener);

        final MetricName name = new MetricName(MetricsRegistryTest.class, "counter1");

        final Counter counter1 = registry.newCounter(MetricsRegistryTest.class, "counter1");
        registry.removeMetric(MetricsRegistryTest.class, "counter1");

        final InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onMetricAdded(name, counter1);
        inOrder.verify(listener).onMetricRemoved(name);
    }

    @Test
    public void createdExecutorsAreShutDownOnShutdown() throws Exception {
        final ScheduledExecutorService service = registry.newScheduledThreadPool(1, "test");

        registry.shutdown();
        
        assertThat(service.isShutdown(),
                   is(true));
    }
}
