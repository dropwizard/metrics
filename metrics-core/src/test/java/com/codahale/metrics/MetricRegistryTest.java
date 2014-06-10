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
    private static final MetricName TIMER2 = MetricName.build("timer");
    private static final MetricName METER2 = MetricName.build("meter");
    private static final MetricName HISTOGRAM2 = MetricName.build("histogram");
    private static final MetricName COUNTER = MetricName.build("counter");
    private static final MetricName COUNTER2 = MetricName.build("counter2");
    private static final MetricName GAUGE = MetricName.build("gauge");
    private static final MetricName GAUGE2 = MetricName.build("gauge2");
    private static final MetricName THING = MetricName.build("thing");
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
        assertThat(registry.register(THING, gauge))
                .isEqualTo(gauge);

        verify(listener).onGaugeAdded(THING, gauge);
    }

    @Test
    public void removingAGaugeTriggersANotification() throws Exception {
        registry.register(THING, gauge);

        assertThat(registry.remove(THING))
                .isTrue();

        verify(listener).onGaugeRemoved(THING);
    }

    @Test
    public void registeringACounterTriggersANotification() throws Exception {
        assertThat(registry.register(THING, counter))
                .isEqualTo(counter);

        verify(listener).onCounterAdded(THING, counter);
    }

    @Test
    public void accessingACounterRegistersAndReusesTheCounter() throws Exception {
        final Counter counter1 = registry.counter(THING);
        final Counter counter2 = registry.counter(THING);

        assertThat(counter1)
                .isSameAs(counter2);

        verify(listener).onCounterAdded(THING, counter1);
    }

    @Test
    public void removingACounterTriggersANotification() throws Exception {
        registry.register(THING, counter);

        assertThat(registry.remove(THING))
                .isTrue();

        verify(listener).onCounterRemoved(THING);
    }

    @Test
    public void registeringAHistogramTriggersANotification() throws Exception {
        assertThat(registry.register(THING, histogram))
                .isEqualTo(histogram);

        verify(listener).onHistogramAdded(THING, histogram);
    }

    @Test
    public void accessingAHistogramRegistersAndReusesIt() throws Exception {
        final Histogram histogram1 = registry.histogram(THING);
        final Histogram histogram2 = registry.histogram(THING);

        assertThat(histogram1)
                .isSameAs(histogram2);

        verify(listener).onHistogramAdded(THING, histogram1);
    }

    @Test
    public void removingAHistogramTriggersANotification() throws Exception {
        registry.register(THING, histogram);

        assertThat(registry.remove(THING))
                .isTrue();

        verify(listener).onHistogramRemoved(THING);
    }

    @Test
    public void registeringAMeterTriggersANotification() throws Exception {
        assertThat(registry.register(THING, meter))
                .isEqualTo(meter);

        verify(listener).onMeterAdded(THING, meter);
    }

    @Test
    public void accessingAMeterRegistersAndReusesIt() throws Exception {
        final Meter meter1 = registry.meter(THING);
        final Meter meter2 = registry.meter(THING);

        assertThat(meter1)
                .isSameAs(meter2);

        verify(listener).onMeterAdded(THING, meter1);
    }

    @Test
    public void removingAMeterTriggersANotification() throws Exception {
        registry.register(THING, meter);

        assertThat(registry.remove(THING))
                .isTrue();

        verify(listener).onMeterRemoved(THING);
    }

    @Test
    public void registeringATimerTriggersANotification() throws Exception {
        assertThat(registry.register(THING, timer))
                .isEqualTo(timer);

        verify(listener).onTimerAdded(THING, timer);
    }

    @Test
    public void accessingATimerRegistersAndReusesIt() throws Exception {
        final Timer timer1 = registry.timer(THING);
        final Timer timer2 = registry.timer(THING);

        assertThat(timer1)
                .isSameAs(timer2);

        verify(listener).onTimerAdded(THING, timer1);
    }

    @Test
    public void removingATimerTriggersANotification() throws Exception {
        registry.register(THING, timer);

        assertThat(registry.remove(THING))
                .isTrue();

        verify(listener).onTimerRemoved(THING);
    }

    @Test
    public void addingAListenerWithExistingMetricsCatchesItUp() throws Exception {
        registry.register(GAUGE2, gauge);
        registry.register(COUNTER2, counter);
        registry.register(HISTOGRAM2, histogram);
        registry.register(METER2, meter);
        registry.register(TIMER2, timer);

        final MetricRegistryListener other = mock(MetricRegistryListener.class);
        registry.addListener(other);

        verify(other).onGaugeAdded(GAUGE2, gauge);
        verify(other).onCounterAdded(COUNTER2, counter);
        verify(other).onHistogramAdded(HISTOGRAM2, histogram);
        verify(other).onMeterAdded(METER2, meter);
        verify(other).onTimerAdded(TIMER2, timer);
    }

    @Test
    public void aRemovedListenerDoesNotReceiveUpdates() throws Exception {
        registry.register(GAUGE, gauge);
        registry.removeListener(listener);
        registry.register(GAUGE2, gauge);

        verify(listener, never()).onGaugeAdded(GAUGE2, gauge);
    }

    @Test
    public void hasAMapOfRegisteredGauges() throws Exception {
        registry.register(GAUGE2, gauge);

        assertThat(registry.getGauges())
                .contains(entry(GAUGE2, gauge));
    }

    @Test
    public void hasAMapOfRegisteredCounters() throws Exception {
        registry.register(COUNTER2, counter);

        assertThat(registry.getCounters())
                .contains(entry(COUNTER2, counter));
    }

    @Test
    public void hasAMapOfRegisteredHistograms() throws Exception {
        registry.register(HISTOGRAM2, histogram);

        assertThat(registry.getHistograms())
                .contains(entry(HISTOGRAM2, histogram));
    }

    @Test
    public void hasAMapOfRegisteredMeters() throws Exception {
        registry.register(METER2, meter);

        assertThat(registry.getMeters())
                .contains(entry(METER2, meter));
    }

    @Test
    public void hasAMapOfRegisteredTimers() throws Exception {
        registry.register(TIMER2, timer);

        assertThat(registry.getTimers())
                .contains(entry(TIMER2, timer));
    }

    @Test
    public void hasASetOfRegisteredMetricNames() throws Exception {
        registry.register(GAUGE2, gauge);
        registry.register(COUNTER2, counter);
        registry.register(HISTOGRAM2, histogram);
        registry.register(METER2, meter);
        registry.register(TIMER2, timer);

        assertThat(registry.getNames())
                .containsOnly(GAUGE2, COUNTER2, HISTOGRAM2, METER2, TIMER2);
    }

    @Test
    public void registersMultipleMetrics() throws Exception {
        final MetricSet metrics = new MetricSet() {
            @Override
            public Map<MetricName, Metric> getMetrics() {
                final Map<MetricName, Metric> metrics = new HashMap<MetricName, Metric>();
                metrics.put(GAUGE2, gauge);
                metrics.put(COUNTER2, counter);
                return metrics;
            }
        };

        registry.registerAll(metrics);

        assertThat(registry.getNames())
                .containsOnly(GAUGE2, COUNTER2);
    }

    @Test
    public void registersMultipleMetricsWithAPrefix() throws Exception {
        final MetricName myCounter = MetricName.build("my.counter");
        final MetricName myGauge = MetricName.build("my.gauge");

        final MetricSet metrics = new MetricSet() {
            @Override
            public Map<MetricName, Metric> getMetrics() {
                final Map<MetricName, Metric> metrics = new HashMap<MetricName, Metric>();
                metrics.put(GAUGE, gauge);
                metrics.put(COUNTER, counter);
                return metrics;
            }
        };

        registry.register("my", metrics);

        assertThat(registry.getNames())
                .containsOnly(myGauge, myCounter);
    }

    @Test
    public void registersRecursiveMetricSets() throws Exception {
        final MetricSet inner = new MetricSet() {
            @Override
            public Map<MetricName, Metric> getMetrics() {
                final Map<MetricName, Metric> metrics = new HashMap<MetricName, Metric>();
                metrics.put(GAUGE, gauge);
                return metrics;
            }
        };

        final MetricSet outer = new MetricSet() {
            @Override
            public Map<MetricName, Metric> getMetrics() {
                final Map<MetricName, Metric> metrics = new HashMap<MetricName, Metric>();
                metrics.put(MetricName.build("inner"), inner);
                metrics.put(COUNTER, counter);
                return metrics;
            }
        };

        registry.register("my", outer);

        final MetricName myCounter = MetricName.build("my.counter");
        final MetricName myInnerGauge = MetricName.build("my.inner.gauge");

        assertThat(registry.getNames())
                .containsOnly(myInnerGauge, myCounter);
    }

    @Test
    public void registersMetricsFromAnotherRegistry() throws Exception {
        MetricRegistry other = new MetricRegistry();
        other.register(GAUGE, gauge);
        registry.register("nested", other);
        assertThat(registry.getNames()).containsOnly(MetricName.build("nested.gauge"));
    }

    @Test
    public void concatenatesStringsToFormADottedName() throws Exception {
        assertThat(name("one", "two", "three"))
                .isEqualTo(MetricName.build("one.two.three"));
    }

    @Test
    @SuppressWarnings("NullArgumentToVariableArgMethod")
    public void elidesNullValuesFromNamesWhenOnlyOneNullPassedIn() throws Exception {
        assertThat(name("one", null))
                .isEqualTo(MetricName.build("one"));
    }

    @Test
    public void elidesNullValuesFromNamesWhenManyNullsPassedIn() throws Exception {
        assertThat(name("one", null, null))
                .isEqualTo(MetricName.build("one"));
    }

    @Test
    public void elidesNullValuesFromNamesWhenNullAndNotNullPassedIn() throws Exception {
        assertThat(name("one", null, "three"))
                .isEqualTo(MetricName.build("one.three"));
    }

    @Test
    public void elidesEmptyStringsFromNames() throws Exception {
        assertThat(name("one", "", "three"))
                .isEqualTo(MetricName.build("one.three"));
    }

    @Test
    public void concatenatesClassNamesWithStringsToFormADottedName() throws Exception {
        assertThat(name(MetricRegistryTest.class, "one", "two"))
                .isEqualTo(MetricName.build("com.codahale.metrics.MetricRegistryTest.one.two"));
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
                .isEqualTo(MetricName.build(g.getClass().getName() + ".one.two"));
    }

    @Test
    public void removesMetricsMatchingAFilter() throws Exception {
        final MetricName timer1 = MetricName.build("timer-1");
        final MetricName timer2 = MetricName.build("timer-2");
        final MetricName histogram1 = MetricName.build("histogram-1");

        registry.timer(timer1);
        registry.timer(timer2);
        registry.histogram(histogram1);

        assertThat(registry.getNames())
                .contains(timer1, timer2, histogram1);

        registry.removeMatching(new MetricFilter() {
            @Override
            public boolean matches(MetricName name, Metric metric) {
                return name.getKey().endsWith("1");
            }
        });

        assertThat(registry.getNames())
                .doesNotContain(timer1, histogram1);
        assertThat(registry.getNames())
                .contains(timer2);

        verify(listener).onTimerRemoved(timer1);
        verify(listener).onHistogramRemoved(histogram1);
    }
}
