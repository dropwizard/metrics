package io.dropwizard.metrics5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class NoopMetricRegistryTest {
    private static final MetricName METRIC_THING = MetricName.build("thing");

    private final MetricRegistryListener listener = mock(MetricRegistryListener.class);
    private final NoopMetricRegistry registry = new NoopMetricRegistry();
    private final Gauge<String> gauge = () -> "";
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);

    @BeforeEach
    void setUp() {
        registry.addListener(listener);
    }

    @Test
    void registeringAGaugeTriggersNoNotification() {
        assertThat(registry.register(METRIC_THING, gauge)).isEqualTo(gauge);

        verify(listener, never()).onGaugeAdded(METRIC_THING, gauge);
    }

    @Test
    void removingAGaugeTriggersNoNotification() {
        registry.register(METRIC_THING, gauge);

        assertThat(registry.remove(METRIC_THING)).isFalse();

        verify(listener, never()).onGaugeRemoved(METRIC_THING);
    }

    @Test
    void registeringACounterTriggersNoNotification() {
        assertThat(registry.register(METRIC_THING, counter)).isEqualTo(counter);

        verify(listener, never()).onCounterAdded(METRIC_THING, counter);
    }

    @Test
    void accessingACounterRegistersAndReusesTheCounter() {
        final Counter counter1 = registry.counter(METRIC_THING);
        final Counter counter2 = registry.counter(METRIC_THING);

        assertThat(counter1).isExactlyInstanceOf(NoopMetricRegistry.NoopCounter.class);
        assertThat(counter2).isExactlyInstanceOf(NoopMetricRegistry.NoopCounter.class);
        assertThat(counter1).isSameAs(counter2);

        verify(listener, never()).onCounterAdded(METRIC_THING, counter1);
    }

    @Test
    void accessingACustomCounterRegistersAndReusesTheCounter() {
        final MetricRegistry.MetricSupplier<Counter> supplier = () -> counter;
        final Counter counter1 = registry.counter(METRIC_THING, supplier);
        final Counter counter2 = registry.counter(METRIC_THING, supplier);

        assertThat(counter1).isExactlyInstanceOf(NoopMetricRegistry.NoopCounter.class);
        assertThat(counter2).isExactlyInstanceOf(NoopMetricRegistry.NoopCounter.class);
        assertThat(counter1).isSameAs(counter2);

        verify(listener, never()).onCounterAdded(METRIC_THING, counter1);
    }


    @Test
    void removingACounterTriggersNoNotification() {
        registry.register(METRIC_THING, counter);

        assertThat(registry.remove(METRIC_THING)).isFalse();

        verify(listener, never()).onCounterRemoved(METRIC_THING);
    }

    @Test
    void registeringAHistogramTriggersNoNotification() {
        assertThat(registry.register(METRIC_THING, histogram)).isEqualTo(histogram);

        verify(listener, never()).onHistogramAdded(METRIC_THING, histogram);
    }

    @Test
    void accessingAHistogramRegistersAndReusesIt() {
        final Histogram histogram1 = registry.histogram(METRIC_THING);
        final Histogram histogram2 = registry.histogram(METRIC_THING);

        assertThat(histogram1).isExactlyInstanceOf(NoopMetricRegistry.NoopHistogram.class);
        assertThat(histogram2).isExactlyInstanceOf(NoopMetricRegistry.NoopHistogram.class);
        assertThat(histogram1).isSameAs(histogram2);

        verify(listener, never()).onHistogramAdded(METRIC_THING, histogram1);
    }

    @Test
    void accessingACustomHistogramRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Histogram> supplier = () -> histogram;
        final Histogram histogram1 = registry.histogram(METRIC_THING, supplier);
        final Histogram histogram2 = registry.histogram(METRIC_THING, supplier);

        assertThat(histogram1).isExactlyInstanceOf(NoopMetricRegistry.NoopHistogram.class);
        assertThat(histogram2).isExactlyInstanceOf(NoopMetricRegistry.NoopHistogram.class);
        assertThat(histogram1).isSameAs(histogram2);

        verify(listener, never()).onHistogramAdded(METRIC_THING, histogram1);
    }

    @Test
    void removingAHistogramTriggersNoNotification() {
        registry.register(METRIC_THING, histogram);

        assertThat(registry.remove(METRIC_THING)).isFalse();

        verify(listener, never()).onHistogramRemoved(METRIC_THING);
    }

    @Test
    void registeringAMeterTriggersNoNotification() {
        assertThat(registry.register(METRIC_THING, meter)).isEqualTo(meter);

        verify(listener, never()).onMeterAdded(METRIC_THING, meter);
    }

    @Test
    void accessingAMeterRegistersAndReusesIt() {
        final Meter meter1 = registry.meter(METRIC_THING);
        final Meter meter2 = registry.meter(METRIC_THING);

        assertThat(meter1).isExactlyInstanceOf(NoopMetricRegistry.NoopMeter.class);
        assertThat(meter2).isExactlyInstanceOf(NoopMetricRegistry.NoopMeter.class);
        assertThat(meter1).isSameAs(meter2);

        verify(listener, never()).onMeterAdded(METRIC_THING, meter1);
    }

    @Test
    void accessingACustomMeterRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Meter> supplier = () -> meter;
        final Meter meter1 = registry.meter(METRIC_THING, supplier);
        final Meter meter2 = registry.meter(METRIC_THING, supplier);

        assertThat(meter1).isExactlyInstanceOf(NoopMetricRegistry.NoopMeter.class);
        assertThat(meter2).isExactlyInstanceOf(NoopMetricRegistry.NoopMeter.class);
        assertThat(meter1).isSameAs(meter2);

        verify(listener, never()).onMeterAdded(METRIC_THING, meter1);
    }

    @Test
    void removingAMeterTriggersNoNotification() {
        registry.register(METRIC_THING, meter);

        assertThat(registry.remove(METRIC_THING)).isFalse();

        verify(listener, never()).onMeterRemoved(METRIC_THING);
    }

    @Test
    void registeringATimerTriggersNoNotification() {
        assertThat(registry.register(METRIC_THING, timer)).isEqualTo(timer);

        verify(listener, never()).onTimerAdded(METRIC_THING, timer);
    }

    @Test
    void accessingATimerRegistersAndReusesIt() {
        final Timer timer1 = registry.timer(METRIC_THING);
        final Timer timer2 = registry.timer(METRIC_THING);

        assertThat(timer1).isExactlyInstanceOf(NoopMetricRegistry.NoopTimer.class);
        assertThat(timer2).isExactlyInstanceOf(NoopMetricRegistry.NoopTimer.class);
        assertThat(timer1).isSameAs(timer2);

        verify(listener, never()).onTimerAdded(METRIC_THING, timer1);
    }

    @Test
    void accessingACustomTimerRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Timer> supplier = () -> timer;
        final Timer timer1 = registry.timer(METRIC_THING, supplier);
        final Timer timer2 = registry.timer(METRIC_THING, supplier);

        assertThat(timer1).isExactlyInstanceOf(NoopMetricRegistry.NoopTimer.class);
        assertThat(timer2).isExactlyInstanceOf(NoopMetricRegistry.NoopTimer.class);
        assertThat(timer1).isSameAs(timer2);

        verify(listener, never()).onTimerAdded(METRIC_THING, timer1);
    }


    @Test
    void removingATimerTriggersNoNotification() {
        registry.register(METRIC_THING, timer);

        assertThat(registry.remove(METRIC_THING)).isFalse();

        verify(listener, never()).onTimerRemoved(METRIC_THING);
    }

    @Test
    void accessingAGaugeRegistersAndReusesIt() {
        final Gauge<Void> gauge1 = registry.gauge(METRIC_THING);
        final Gauge<Void> gauge2 = registry.gauge(METRIC_THING);

        assertThat(gauge1).isExactlyInstanceOf(NoopMetricRegistry.NoopGauge.class);
        assertThat(gauge2).isExactlyInstanceOf(NoopMetricRegistry.NoopGauge.class);
        assertThat(gauge1).isSameAs(gauge2);

        verify(listener, never()).onGaugeAdded(METRIC_THING, gauge1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void accessingACustomGaugeRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Gauge<String>> supplier = () -> gauge;
        final Gauge gauge1 = registry.gauge(METRIC_THING, supplier);
        final Gauge gauge2 = registry.gauge(METRIC_THING, supplier);

        assertThat(gauge1).isExactlyInstanceOf(NoopMetricRegistry.NoopGauge.class);
        assertThat(gauge2).isExactlyInstanceOf(NoopMetricRegistry.NoopGauge.class);
        assertThat(gauge1).isSameAs(gauge2);

        verify(listener, never()).onGaugeAdded(METRIC_THING, gauge1);
    }


    @Test
    void addingAListenerWithExistingMetricsDoesNotNotify() {
        registry.register(MetricName.build("gauge"), gauge);
        registry.register(MetricName.build("counter"), counter);
        registry.register(MetricName.build("histogram"), histogram);
        registry.register(MetricName.build("meter"), meter);
        registry.register(MetricName.build("timer"), timer);

        final MetricRegistryListener other = mock(MetricRegistryListener.class);
        registry.addListener(other);

        verify(other, never()).onGaugeAdded(MetricName.build("gauge"), gauge);
        verify(other, never()).onCounterAdded(MetricName.build("counter"), counter);
        verify(other, never()).onHistogramAdded(MetricName.build("histogram"), histogram);
        verify(other, never()).onMeterAdded(MetricName.build("meter"), meter);
        verify(other, never()).onTimerAdded(MetricName.build("timer"), timer);
    }

    @Test
    void aRemovedListenerDoesNotReceiveUpdates() {
        registry.register(MetricName.build("gauge"), gauge);
        registry.removeListener(listener);
        registry.register(MetricName.build("gauge2"), gauge);

        verify(listener, never()).onGaugeAdded(MetricName.build("gauge2"), gauge);
    }

    @Test
    void hasAMapOfRegisteredGauges() {
        registry.register(MetricName.build("gauge"), gauge);

        assertThat(registry.getGauges()).isEmpty();
    }

    @Test
    void hasAMapOfRegisteredCounters() {
        registry.register(MetricName.build("counter"), counter);

        assertThat(registry.getCounters()).isEmpty();
    }

    @Test
    void hasAMapOfRegisteredHistograms() {
        registry.register(MetricName.build("histogram"), histogram);

        assertThat(registry.getHistograms()).isEmpty();
    }

    @Test
    void hasAMapOfRegisteredMeters() {
        registry.register(MetricName.build("meter"), meter);

        assertThat(registry.getMeters()).isEmpty();
    }

    @Test
    void hasAMapOfRegisteredTimers() {
        registry.register(MetricName.build("timer"), timer);

        assertThat(registry.getTimers()).isEmpty();
    }

    @Test
    void hasASetOfRegisteredMetricNames() {
        registry.register(MetricName.build("gauge"), gauge);
        registry.register(MetricName.build("counter"), counter);
        registry.register(MetricName.build("histogram"), histogram);
        registry.register(MetricName.build("meter"), meter);
        registry.register(MetricName.build("timer"), timer);

        assertThat(registry.getNames()).isEmpty();
    }

    @Test
    void doesNotRegisterMultipleMetrics() {
        final MetricSet metrics = () -> {
            final Map<MetricName, Metric> m = new HashMap<>();
            m.put(MetricName.build("gauge"), gauge);
            m.put(MetricName.build("counter"), counter);
            return m;
        };

        registry.registerAll(metrics);

        assertThat(registry.getNames()).isEmpty();
    }

    @Test
    void doesNotRegisterMultipleMetricsWithAPrefix() {
        final MetricSet metrics = () -> {
            final Map<MetricName, Metric> m = new HashMap<>();
            m.put(MetricName.build("gauge"), gauge);
            m.put(MetricName.build("counter"), counter);
            return m;
        };

        registry.register(MetricName.build("my"), metrics);

        assertThat(registry.getNames()).isEmpty();
    }

    @Test
    void doesNotRegisterRecursiveMetricSets() {
        final MetricSet inner = () -> {
            final Map<MetricName, Metric> m = new HashMap<>();
            m.put(MetricName.build("gauge"), gauge);
            return m;
        };

        final MetricSet outer = () -> {
            final Map<MetricName, Metric> m = new HashMap<>();
            m.put(MetricName.build("inner"), inner);
            m.put(MetricName.build("counter"), counter);
            return m;
        };

        registry.register(MetricName.build("my"), outer);

        assertThat(registry.getNames()).isEmpty();
    }

    @Test
    void doesNotRegisterMetricsFromAnotherRegistry() {
        MetricRegistry other = new MetricRegistry();
        other.register(MetricName.build("gauge"), gauge);
        registry.register(MetricName.build("nested"), other);
        assertThat(registry.getNames()).isEmpty();
    }

    @Test
    void removesMetricsMatchingAFilter() {
        registry.timer("timer-1");
        registry.timer("timer-2");
        registry.histogram("histogram-1");

        assertThat(registry.getNames()).isEmpty();

        registry.removeMatching((name, metric) -> name.getKey().endsWith("1"));

        assertThat(registry.getNames()).isEmpty();

        verify(listener, never()).onTimerRemoved(MetricName.build("timer-1"));
        verify(listener, never()).onHistogramRemoved(MetricName.build("histogram-1"));
    }

    @Test
    void addingChildMetricAfterRegister() {
        MetricRegistry parent = new NoopMetricRegistry();
        MetricRegistry child = new MetricRegistry();

        child.counter(MetricName.build("test-1"));
        parent.register(MetricName.build("child"), child);
        child.counter(MetricName.build("test-2"));

        assertThat(parent.getMetrics()).isEmpty();
    }

    @Test
    void addingMultipleChildMetricsAfterRegister() {
        MetricRegistry parent = new NoopMetricRegistry();
        MetricRegistry child = new MetricRegistry();

        child.counter(MetricName.build("test-1"));
        child.counter(MetricName.build("test-2"));
        parent.register(MetricName.build("child"), child);
        child.counter(MetricName.build("test-3"));
        child.counter(MetricName.build("test-4"));

        assertThat(parent.getMetrics()).isEmpty();
    }

    @Test
    void addingDeepChildMetricsAfterRegister() {
        MetricRegistry parent = new NoopMetricRegistry();
        MetricRegistry child = new MetricRegistry();
        MetricRegistry deepChild = new MetricRegistry();

        deepChild.counter(MetricName.build("test-1"));
        child.register(MetricName.build("deep-child"), deepChild);
        deepChild.counter(MetricName.build("test-2"));

        child.counter(MetricName.build("test-3"));
        parent.register(MetricName.build("child"), child);
        child.counter(MetricName.build("test-4"));

        deepChild.counter("test-5");

        assertThat(parent.getMetrics()).isEmpty();
        assertThat(deepChild.getMetrics()).hasSize(3);
        assertThat(child.getMetrics()).hasSize(5);
    }

    @Test
    void removingChildMetricAfterRegister() {
        MetricRegistry parent = new NoopMetricRegistry();
        MetricRegistry child = new MetricRegistry();

        child.counter(MetricName.build("test-1"));
        parent.register(MetricName.build("child"), child);
        child.counter(MetricName.build("test-2"));

        child.remove(MetricName.build("test-1"));

        assertThat(parent.getMetrics()).isEmpty();
        assertThat(child.getMetrics()).doesNotContainKey(MetricName.build("test-1"));
    }

    @Test
    void removingMultipleChildMetricsAfterRegister() {
        MetricRegistry parent = new NoopMetricRegistry();
        MetricRegistry child = new MetricRegistry();

        child.counter(MetricName.build("test-1"));
        child.counter(MetricName.build("test-2"));
        parent.register(MetricName.build("child"), child);
        child.counter(MetricName.build("test-3"));
        child.counter(MetricName.build("test-4"));

        child.remove(MetricName.build("test-1"));
        child.remove(MetricName.build("test-3"));

        assertThat(parent.getMetrics()).isEmpty();
        assertThat(child.getMetrics()).doesNotContainKeys(MetricName.build("test-1"), MetricName.build("test-3"));
    }

    @Test
    void removingDeepChildMetricsAfterRegister() {
        MetricRegistry parent = new NoopMetricRegistry();
        MetricRegistry child = new MetricRegistry();
        MetricRegistry deepChild = new MetricRegistry();

        deepChild.counter(MetricName.build("test-1"));
        child.register(MetricName.build("deep-child"), deepChild);
        deepChild.counter(MetricName.build("test-2"));

        child.counter(MetricName.build("test-3"));
        parent.register(MetricName.build("child"), child);
        child.counter(MetricName.build("test-4"));

        deepChild.remove(MetricName.build("test-2"));

        Set<MetricName> childMetrics = child.getMetrics().keySet();
        Set<MetricName> deepChildMetrics = deepChild.getMetrics().keySet();

        assertThat(parent.getMetrics()).isEmpty();
        assertThat(deepChildMetrics).hasSize(1);
        assertThat(childMetrics).hasSize(3);
    }

    @Test
    void registerNullMetric() {
        MetricRegistry registry = new NoopMetricRegistry();
        assertThatNullPointerException()
                .isThrownBy(() -> registry.register(MetricName.build("any_name"), null))
                .withMessage("metric == null");
    }

    @Test
    void timesRunnableInstances() {
        Timer timer = registry.timer("thing");
        final AtomicBoolean called = new AtomicBoolean();
        timer.time(() -> called.set(true));

        assertThat(called).isTrue();
    }
}
