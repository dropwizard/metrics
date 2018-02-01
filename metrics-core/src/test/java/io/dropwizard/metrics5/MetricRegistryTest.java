package io.dropwizard.metrics5;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.dropwizard.metrics5.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class MetricRegistryTest {

    private static class CustomCounter extends Counter {

        CustomCounter() {
            super();
        }

        public void incTheAnswer() {
            inc(42);
        }
    }

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
    private final Gauge<String> gauge = () -> "";
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);
    @Before
    public void setUp() {
        registry.addListener(listener);
    }

    @Test
    public void registeringAGaugeTriggersANotification() {
        assertThat(registry.register(THING, gauge))
                .isEqualTo(gauge);

        verify(listener).onGaugeAdded(THING, gauge);
    }

    @Test
    public void removingAGaugeTriggersANotification() {
        registry.register(THING, gauge);

        assertThat(registry.remove(THING))
                .isTrue();

        verify(listener).onGaugeRemoved(THING);
    }

    @Test
    public void registeringACounterTriggersANotification() {
        assertThat(registry.register(THING, counter))
                .isEqualTo(counter);

        verify(listener).onCounterAdded(THING, counter);
    }

    @Test
    public void accessingACounterRegistersAndReusesTheCounter() {
        final Counter counter1 = registry.counter(THING);
        final Counter counter2 = registry.counter(THING);

        assertThat(counter1)
                .isSameAs(counter2);

        verify(listener).onCounterAdded(THING, counter1);
    }

    @Test
    public void accessingACustomCounterRegistersAndReusesTheCounter() {
        final MetricRegistry.MetricSupplier<Counter> supplier = () -> counter;
        final Counter counter1 = registry.counter(THING, supplier);
        final Counter counter2 = registry.counter(THING, supplier);

        assertThat(counter1)
                .isSameAs(counter2);

        verify(listener).onCounterAdded(THING, counter1);
    }

    @Test
    public void createsTypesafeCustomCounter() {
        MetricName name = MetricName.build("custom-counter");
        final CustomCounter customCounter = registry.counter(name, CustomCounter::new);
        customCounter.incTheAnswer();

        assertThat(registry.counter(name).getCount()).isEqualTo(42);
    }

    @Test
    public void removingACounterTriggersANotification() {
        registry.register(THING, counter);

        assertThat(registry.remove(THING))
                .isTrue();

        verify(listener).onCounterRemoved(THING);
    }

    @Test
    public void registeringAHistogramTriggersANotification() {
        assertThat(registry.register(THING, histogram))
                .isEqualTo(histogram);

        verify(listener).onHistogramAdded(THING, histogram);
    }

    @Test
    public void accessingAHistogramRegistersAndReusesIt() {
        final Histogram histogram1 = registry.histogram(THING);
        final Histogram histogram2 = registry.histogram(THING);

        assertThat(histogram1)
                .isSameAs(histogram2);

        verify(listener).onHistogramAdded(THING, histogram1);
    }

    @Test
    public void accessingACustomHistogramRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Histogram> supplier = () -> histogram;
        final Histogram histogram1 = registry.histogram(THING, supplier);
        final Histogram histogram2 = registry.histogram(THING, supplier);

        assertThat(histogram1)
                .isSameAs(histogram2);

        verify(listener).onHistogramAdded(THING, histogram1);
    }

    @Test
    public void removingAHistogramTriggersANotification() {
        registry.register(THING, histogram);

        assertThat(registry.remove(THING))
                .isTrue();

        verify(listener).onHistogramRemoved(THING);
    }

    @Test
    public void registeringAMeterTriggersANotification() {
        assertThat(registry.register(THING, meter))
                .isEqualTo(meter);

        verify(listener).onMeterAdded(THING, meter);
    }

    @Test
    public void accessingAMeterRegistersAndReusesIt() {
        final Meter meter1 = registry.meter(THING);
        final Meter meter2 = registry.meter(THING);

        assertThat(meter1)
                .isSameAs(meter2);

        verify(listener).onMeterAdded(THING, meter1);
    }

    @Test
    public void accessingACustomMeterRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Meter> supplier = () -> meter;
        final Meter meter1 = registry.meter(THING, supplier);
        final Meter meter2 = registry.meter(THING, supplier);

        assertThat(meter1)
                .isSameAs(meter2);

        verify(listener).onMeterAdded(THING, meter1);
    }

    @Test
    public void removingAMeterTriggersANotification() {
        registry.register(THING, meter);

        assertThat(registry.remove(THING))
                .isTrue();

        verify(listener).onMeterRemoved(THING);
    }

    @Test
    public void registeringATimerTriggersANotification() {
        assertThat(registry.register(THING, timer))
                .isEqualTo(timer);

        verify(listener).onTimerAdded(THING, timer);
    }

    @Test
    public void accessingATimerRegistersAndReusesIt() {
        final Timer timer1 = registry.timer(THING);
        final Timer timer2 = registry.timer(THING);

        assertThat(timer1)
                .isSameAs(timer2);

        verify(listener).onTimerAdded(THING, timer1);
    }

    @Test
    public void accessingACustomTimerRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Timer> supplier = () -> timer;
        final Timer timer1 = registry.timer(THING, supplier);
        final Timer timer2 = registry.timer(THING, supplier);

        assertThat(timer1)
                .isSameAs(timer2);

        verify(listener).onTimerAdded(THING, timer1);
    }


    @Test
    public void removingATimerTriggersANotification() {
        registry.register(THING, timer);

        assertThat(registry.remove(THING))
                .isTrue();

        verify(listener).onTimerRemoved(THING);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void accessingACustomGaugeRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Gauge> supplier = () -> gauge;
        final Gauge gauge1 = registry.gauge(THING, supplier);
        final Gauge gauge2 = registry.gauge(THING, supplier);

        assertThat(gauge1)
                .isSameAs(gauge2);

        verify(listener).onGaugeAdded(THING, gauge1);
    }


    @Test
    public void addingAListenerWithExistingMetricsCatchesItUp() {
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
    public void aRemovedListenerDoesNotReceiveUpdates() {
        registry.register(GAUGE, gauge);
        registry.removeListener(listener);
        registry.register(GAUGE2, gauge);

        verify(listener, never()).onGaugeAdded(GAUGE2, gauge);
    }

    @Test
    public void hasAMapOfRegisteredGauges() {
        registry.register(GAUGE2, gauge);

        assertThat(registry.getGauges())
                .contains(entry(GAUGE2, gauge));
    }

    @Test
    public void hasAMapOfRegisteredCounters() {
        registry.register(COUNTER2, counter);

        assertThat(registry.getCounters())
                .contains(entry(COUNTER2, counter));
    }

    @Test
    public void hasAMapOfRegisteredHistograms() {
        registry.register(HISTOGRAM2, histogram);

        assertThat(registry.getHistograms())
                .contains(entry(HISTOGRAM2, histogram));
    }

    @Test
    public void hasAMapOfRegisteredMeters() {
        registry.register(METER2, meter);

        assertThat(registry.getMeters())
                .contains(entry(METER2, meter));
    }

    @Test
    public void hasAMapOfRegisteredTimers() {
        registry.register(TIMER2, timer);

        assertThat(registry.getTimers())
                .contains(entry(TIMER2, timer));
    }

    @Test
    public void hasASetOfRegisteredMetricNames() {
        registry.register(GAUGE2, gauge);
        registry.register(COUNTER2, counter);
        registry.register(HISTOGRAM2, histogram);
        registry.register(METER2, meter);
        registry.register(TIMER2, timer);

        assertThat(registry.getNames())
                .containsOnly(GAUGE2, COUNTER2, HISTOGRAM2, METER2, TIMER2);
    }

    @Test
    public void registersMultipleMetrics() {
        final MetricSet metrics = () -> {
            final Map<MetricName, Metric> m = new HashMap<>();
            m.put(GAUGE2, gauge);
            m.put(COUNTER2, counter);
            return m;
        };

        registry.registerAll(metrics);

        assertThat(registry.getNames())
                .containsOnly(GAUGE2, COUNTER2);
    }

    @Test
    public void registersMultipleMetricsWithAPrefix() {
        final MetricName myCounter = MetricName.build("my.counter");
        final MetricName myGauge = MetricName.build("my.gauge");

        final MetricSet metrics = () -> {
            final Map<MetricName, Metric> m = new HashMap<>();
            m.put(GAUGE, gauge);
            m.put(COUNTER, counter);
            return m;
        };

        registry.register("my", metrics);

        assertThat(registry.getNames())
                .containsOnly(myGauge, myCounter);
    }

    @Test
    public void registersRecursiveMetricSets() {
        final MetricSet inner = () -> {
            final Map<MetricName, Metric> m = new HashMap<>();
            m.put(GAUGE, gauge);
            return m;
        };

        final MetricSet outer = () -> {
            final Map<MetricName, Metric> m = new HashMap<>();
            m.put(MetricName.build("inner"), inner);
            m.put(COUNTER, counter);
            return m;
        };

        registry.register("my", outer);

        final MetricName myCounter = MetricName.build("my.counter");
        final MetricName myInnerGauge = MetricName.build("my.inner.gauge");

        assertThat(registry.getNames())
                .containsOnly(myInnerGauge, myCounter);
    }

    @Test
    public void registersMetricsFromAnotherRegistry() {
        MetricRegistry other = new MetricRegistry();
        other.register(GAUGE, gauge);
        registry.register("nested", other);
        assertThat(registry.getNames()).containsOnly(MetricName.build("nested.gauge"));
    }

    @Test
    public void concatenatesStringsToFormADottedName() {
        assertThat(name("one", "two", "three"))
                .isEqualTo(MetricName.build("one.two.three"));
    }

    @Test
    @SuppressWarnings("NullArgumentToVariableArgMethod")
    public void elidesNullValuesFromNamesWhenOnlyOneNullPassedIn() {
        assertThat(name("one", (String) null))
                .isEqualTo(MetricName.build("one"));
    }

    @Test
    public void elidesNullValuesFromNamesWhenManyNullsPassedIn() {
        assertThat(name("one", null, null))
                .isEqualTo(MetricName.build("one"));
    }

    @Test
    public void elidesNullValuesFromNamesWhenNullAndNotNullPassedIn() {
        assertThat(name("one", null, "three"))
                .isEqualTo(MetricName.build("one.three"));
    }

    @Test
    public void elidesEmptyStringsFromNames() {
        assertThat(name("one", "", "three"))
                .isEqualTo(MetricName.build("one.three"));
    }

    @Test
    public void concatenatesClassNamesWithStringsToFormADottedName() {
        assertThat(name(MetricRegistryTest.class, "one", "two"))
                .isEqualTo(MetricName.build("io.dropwizard.metrics5.MetricRegistryTest.one.two"));
    }

    @Test
    public void concatenatesClassesWithoutCanonicalNamesWithStrings() {
        final Gauge<String> g = () -> null;

        assertThat(name(g.getClass(), "one", "two"))
                .isEqualTo(MetricName.build(g.getClass().getName() + ".one.two"));
    }

    @Test
    public void removesMetricsMatchingAFilter() {
        final MetricName timer1 = MetricName.build("timer-1");
        final MetricName timer2 = MetricName.build("timer-2");
        final MetricName histogram1 = MetricName.build("histogram-1");

        registry.timer(timer1);
        registry.timer(timer2);
        registry.histogram(histogram1);

        assertThat(registry.getNames())
                .contains(timer1, timer2, histogram1);

        registry.removeMatching((name, metric) -> name.getKey().endsWith("1"));

        assertThat(registry.getNames())
                .doesNotContain(timer1, histogram1);
        assertThat(registry.getNames())
                .contains(timer2);

        verify(listener).onTimerRemoved(timer1);
        verify(listener).onHistogramRemoved(histogram1);
    }
}
