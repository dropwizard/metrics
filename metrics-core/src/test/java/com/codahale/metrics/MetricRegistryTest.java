package com.codahale.metrics;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.data.MapEntry.entry;
import static org.mockito.Mockito.*;

public class MetricRegistryTest {
    private final MetricRegistryListener listener = mock(MetricRegistryListener.class);
    private final MetricRegistry registry = new MetricRegistry();
    @SuppressWarnings("unchecked")
    private final Gauge<String> gauge = mock(Gauge.class);
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);

    @Before
    public void setUp() throws Exception {
        registry.addListener(listener);
    }

    @Test
    public void registeringAGaugeTriggersANotification() throws Exception {
        assertThat(registry.register("thing", gauge))
                .isEqualTo(gauge);

        verify(listener).onGaugeAdded("thing", gauge);
    }

    @Test
    public void removingAGaugeTriggersANotification() throws Exception {
        registry.register("thing", gauge);

        assertThat(registry.remove("thing"))
                .isTrue();

        verify(listener).onGaugeRemoved("thing");
    }

    @Test
    public void registeringACounterTriggersANotification() throws Exception {
        assertThat(registry.register("thing", counter))
                .isEqualTo(counter);

        verify(listener).onCounterAdded("thing", counter);
    }

    @Test
    public void accessingACounterRegistersAndReusesTheCounter() throws Exception {
        final Counter counter1 = registry.counter("thing");
        final Counter counter2 = registry.counter("thing");

        assertThat(counter1)
                .isSameAs(counter2);

        verify(listener).onCounterAdded("thing", counter1);
    }

    @Test
    public void removingACounterTriggersANotification() throws Exception {
        registry.register("thing", counter);

        assertThat(registry.remove("thing"))
                .isTrue();

        verify(listener).onCounterRemoved("thing");
    }

    @Test
    public void registeringAHistogramTriggersANotification() throws Exception {
        assertThat(registry.register("thing", histogram))
                .isEqualTo(histogram);

        verify(listener).onHistogramAdded("thing", histogram);
    }

    @Test
    public void accessingAHistogramRegistersAndReusesIt() throws Exception {
        final Histogram histogram1 = registry.histogram("thing");
        final Histogram histogram2 = registry.histogram("thing");

        assertThat(histogram1)
                .isSameAs(histogram2);

        verify(listener).onHistogramAdded("thing", histogram1);
    }

    @Test
    public void removingAHistogramTriggersANotification() throws Exception {
        registry.register("thing", histogram);

        assertThat(registry.remove("thing"))
                .isTrue();

        verify(listener).onHistogramRemoved("thing");
    }

    @Test
    public void registeringAMeterTriggersANotification() throws Exception {
        assertThat(registry.register("thing", meter))
                .isEqualTo(meter);

        verify(listener).onMeterAdded("thing", meter);
    }

    @Test
    public void accessingAMeterRegistersAndReusesIt() throws Exception {
        final Meter meter1 = registry.meter("thing");
        final Meter meter2 = registry.meter("thing");

        assertThat(meter1)
                .isSameAs(meter2);

        verify(listener).onMeterAdded("thing", meter1);
    }

    @Test
    public void removingAMeterTriggersANotification() throws Exception {
        registry.register("thing", meter);

        assertThat(registry.remove("thing"))
                .isTrue();

        verify(listener).onMeterRemoved("thing");
    }

    @Test
    public void registeringATimerTriggersANotification() throws Exception {
        assertThat(registry.register("thing", timer))
                .isEqualTo(timer);

        verify(listener).onTimerAdded("thing", timer);
    }

    @Test
    public void accessingATimerRegistersAndReusesIt() throws Exception {
        final Timer timer1 = registry.timer("thing");
        final Timer timer2 = registry.timer("thing");

        assertThat(timer1)
                .isSameAs(timer2);

        verify(listener).onTimerAdded("thing", timer1);
    }

    @Test
    public void removingATimerTriggersANotification() throws Exception {
        registry.register("thing", timer);

        assertThat(registry.remove("thing"))
                .isTrue();

        verify(listener).onTimerRemoved("thing");
    }

    @Test
    public void addingAListenerWithExistingMetricsCatchesItUp() throws Exception {
        registry.register("gauge", gauge);
        registry.register("counter", counter);
        registry.register("histogram", histogram);
        registry.register("meter", meter);
        registry.register("timer", timer);

        final MetricRegistryListener other = mock(MetricRegistryListener.class);
        registry.addListener(other);

        verify(other).onGaugeAdded("gauge", gauge);
        verify(other).onCounterAdded("counter", counter);
        verify(other).onHistogramAdded("histogram", histogram);
        verify(other).onMeterAdded("meter", meter);
        verify(other).onTimerAdded("timer", timer);
    }

    @Test
    public void aRemovedListenerDoesNotReceiveUpdates() throws Exception {
        registry.register("gauge", gauge);
        registry.removeListener(listener);
        registry.register("gauge2", gauge);

        verify(listener, never()).onGaugeAdded("gauge2", gauge);
    }

    @Test
    public void hasAMapOfRegisteredGauges() throws Exception {
        registry.register("gauge", gauge);

        assertThat(registry.getGauges())
                .contains(entry("gauge", gauge));
    }

    @Test
    public void hasAMapOfRegisteredCounters() throws Exception {
        registry.register("counter", counter);

        assertThat(registry.getCounters())
                .contains(entry("counter", counter));
    }

    @Test
    public void hasAMapOfRegisteredHistograms() throws Exception {
        registry.register("histogram", histogram);

        assertThat(registry.getHistograms())
                .contains(entry("histogram", histogram));
    }

    @Test
    public void hasAMapOfRegisteredMeters() throws Exception {
        registry.register("meter", meter);

        assertThat(registry.getMeters())
                .contains(entry("meter", meter));
    }

    @Test
    public void hasAMapOfRegisteredTimers() throws Exception {
        registry.register("timer", timer);

        assertThat(registry.getTimers())
                .contains(entry("timer", timer));
    }

    @Test
    public void hasASetOfRegisteredMetricNames() throws Exception {
        registry.register("gauge", gauge);
        registry.register("counter", counter);
        registry.register("histogram", histogram);
        registry.register("meter", meter);
        registry.register("timer", timer);

        assertThat(registry.getNames())
                .containsOnly("gauge", "counter", "histogram", "meter", "timer");
    }

    @Test
    public void registersMultipleMetrics() throws Exception {
        final MetricSet metrics = new MetricSet() {
            @Override
            public Map<String, Metric> getMetrics() {
                final Map<String, Metric> metrics = new HashMap<String, Metric>();
                metrics.put("gauge", gauge);
                metrics.put("counter", counter);
                return metrics;
            }
        };

        registry.registerAll(metrics);

        assertThat(registry.getNames())
                .containsOnly("gauge", "counter");
    }

    @Test
    public void registersMultipleMetricsWithAPrefix() throws Exception {
        final MetricSet metrics = new MetricSet() {
            @Override
            public Map<String, Metric> getMetrics() {
                final Map<String, Metric> metrics = new HashMap<String, Metric>();
                metrics.put("gauge", gauge);
                metrics.put("counter", counter);
                return metrics;
            }
        };

        registry.register("my", metrics);

        assertThat(registry.getNames())
                .containsOnly("my.gauge", "my.counter");
    }

    @Test
    public void registersRecursiveMetricSets() throws Exception {
        final MetricSet inner = new MetricSet() {
            @Override
            public Map<String, Metric> getMetrics() {
                final Map<String, Metric> metrics = new HashMap<String, Metric>();
                metrics.put("gauge", gauge);
                return metrics;
            }
        };

        final MetricSet outer = new MetricSet() {
            @Override
            public Map<String, Metric> getMetrics() {
                final Map<String, Metric> metrics = new HashMap<String, Metric>();
                metrics.put("inner", inner);
                metrics.put("counter", counter);
                return metrics;
            }
        };

        registry.register("my", outer);

        assertThat(registry.getNames())
                .containsOnly("my.inner.gauge", "my.counter");
    }

    @Test
    public void registersMetricsFromAnotherRegistry() throws Exception {
        MetricRegistry other = new MetricRegistry();
        other.register("gauge", gauge);
        registry.register("nested", other);
        assertThat(registry.getNames()).containsOnly("nested.gauge");
    }

    @Test
    public void concatenatesStringsToFormADottedName() throws Exception {
        assertThat(name("one", "two", "three"))
                .isEqualTo("one.two.three");
    }

    @Test
    @SuppressWarnings("NullArgumentToVariableArgMethod")
    public void elidesNullValuesFromNamesWhenOnlyOneNullPassedIn() throws Exception {
        assertThat(name("one", (String)null))
                .isEqualTo("one");
    }

    @Test
    public void elidesNullValuesFromNamesWhenManyNullsPassedIn() throws Exception {
        assertThat(name("one", null, null))
                .isEqualTo("one");
    }

    @Test
    public void elidesNullValuesFromNamesWhenNullAndNotNullPassedIn() throws Exception {
        assertThat(name("one", null, "three"))
                .isEqualTo("one.three");
    }

    @Test
    public void elidesEmptyStringsFromNames() throws Exception {
        assertThat(name("one", "", "three"))
                .isEqualTo("one.three");
    }

    @Test
    public void concatenatesClassNamesWithStringsToFormADottedName() throws Exception {
        assertThat(name(MetricRegistryTest.class, "one", "two"))
                .isEqualTo("com.codahale.metrics.MetricRegistryTest.one.two");
    }

    @Test
    public void concatenatesClassesWithoutCanonicalNamesWithStrings() throws Exception {
        final Gauge<String> g = new Gauge<String>() {
            @Override
            public String getValue() {
                return null;
            }
        };

        assertThat(name(g.getClass(), "one", "two"))
                .isEqualTo("com.codahale.metrics.MetricRegistryTest$5.one.two");
    }

    @Test
    public void removesMetricsMatchingAFilter() throws Exception {
        registry.timer("timer-1");
        registry.timer("timer-2");
        registry.histogram("histogram-1");

        assertThat(registry.getNames())
                .contains("timer-1", "timer-2", "histogram-1");

        registry.removeMatching(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.endsWith("1");
            }
        });

        assertThat(registry.getNames())
                .doesNotContain("timer-1", "histogram-1");
        assertThat(registry.getNames())
                .contains("timer-2");

        verify(listener).onTimerRemoved("timer-1");
        verify(listener).onHistogramRemoved("histogram-1");
    }
}
