package io.dropwizard.metrics5;

import io.dropwizard.metrics5.NoopMetricRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class NoopMetricRegistryTest {
    private final MetricRegistryListener listener = mock(MetricRegistryListener.class);
    private final NoopMetricRegistry registry = new NoopMetricRegistry();
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
    public void registeringAGaugeTriggersNoNotification() {
        assertThat(registry.register(MetricName.build("thing"), gauge)).isEqualTo(gauge);

        verify(listener, never()).onGaugeAdded(MetricName.build("thing"), gauge);
    }

    @Test
    public void removingAGaugeTriggersNoNotification() {
        registry.register(MetricName.build("thing"), gauge);

        assertThat(registry.remove(MetricName.build("thing"))).isFalse();

        verify(listener, never()).onGaugeRemoved(MetricName.build("thing"));
    }

    @Test
    public void registeringACounterTriggersNoNotification() {
        assertThat(registry.register(MetricName.build("thing"), counter)).isEqualTo(counter);

        verify(listener, never()).onCounterAdded(MetricName.build("thing"), counter);
    }

    @Test
    public void accessingACounterRegistersAndReusesTheCounter() {
        final Counter counter1 = registry.counter(MetricName.build("thing"));
        final Counter counter2 = registry.counter(MetricName.build("thing"));

        assertThat(counter1).isExactlyInstanceOf(NoopMetricRegistry.NoopCounter.class);
        assertThat(counter2).isExactlyInstanceOf(NoopMetricRegistry.NoopCounter.class);
        assertThat(counter1).isSameAs(counter2);

        verify(listener, never()).onCounterAdded(MetricName.build("thing"), counter1);
    }

    @Test
    public void accessingACustomCounterRegistersAndReusesTheCounter() {
        final MetricRegistry.MetricSupplier<Counter> supplier = () -> counter;
        final Counter counter1 = registry.counter(MetricName.build("thing"), supplier);
        final Counter counter2 = registry.counter(MetricName.build("thing"), supplier);

        assertThat(counter1).isExactlyInstanceOf(NoopMetricRegistry.NoopCounter.class);
        assertThat(counter2).isExactlyInstanceOf(NoopMetricRegistry.NoopCounter.class);
        assertThat(counter1).isSameAs(counter2);

        verify(listener, never()).onCounterAdded(MetricName.build("thing"), counter1);
    }


    @Test
    public void removingACounterTriggersNoNotification() {
        registry.register(MetricName.build("thing"), counter);

        assertThat(registry.remove(MetricName.build("thing"))).isFalse();

        verify(listener, never()).onCounterRemoved(MetricName.build("thing"));
    }

    @Test
    public void registeringAHistogramTriggersNoNotification() {
        assertThat(registry.register(MetricName.build("thing"), histogram)).isEqualTo(histogram);

        verify(listener, never()).onHistogramAdded(MetricName.build("thing"), histogram);
    }

    @Test
    public void accessingAHistogramRegistersAndReusesIt() {
        final Histogram histogram1 = registry.histogram(MetricName.build("thing"));
        final Histogram histogram2 = registry.histogram(MetricName.build("thing"));

        assertThat(histogram1).isExactlyInstanceOf(NoopMetricRegistry.NoopHistogram.class);
        assertThat(histogram2).isExactlyInstanceOf(NoopMetricRegistry.NoopHistogram.class);
        assertThat(histogram1).isSameAs(histogram2);

        verify(listener, never()).onHistogramAdded(MetricName.build("thing"), histogram1);
    }

    @Test
    public void accessingACustomHistogramRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Histogram> supplier = () -> histogram;
        final Histogram histogram1 = registry.histogram(MetricName.build("thing"), supplier);
        final Histogram histogram2 = registry.histogram(MetricName.build("thing"), supplier);

        assertThat(histogram1).isExactlyInstanceOf(NoopMetricRegistry.NoopHistogram.class);
        assertThat(histogram2).isExactlyInstanceOf(NoopMetricRegistry.NoopHistogram.class);
        assertThat(histogram1).isSameAs(histogram2);

        verify(listener, never()).onHistogramAdded(MetricName.build("thing"), histogram1);
    }

    @Test
    public void removingAHistogramTriggersNoNotification() {
        registry.register(MetricName.build("thing"), histogram);

        assertThat(registry.remove(MetricName.build("thing"))).isFalse();

        verify(listener, never()).onHistogramRemoved(MetricName.build("thing"));
    }

    @Test
    public void registeringAMeterTriggersNoNotification() {
        assertThat(registry.register(MetricName.build("thing"), meter)).isEqualTo(meter);

        verify(listener, never()).onMeterAdded(MetricName.build("thing"), meter);
    }

    @Test
    public void accessingAMeterRegistersAndReusesIt() {
        final Meter meter1 = registry.meter(MetricName.build("thing"));
        final Meter meter2 = registry.meter(MetricName.build("thing"));

        assertThat(meter1).isExactlyInstanceOf(NoopMetricRegistry.NoopMeter.class);
        assertThat(meter2).isExactlyInstanceOf(NoopMetricRegistry.NoopMeter.class);
        assertThat(meter1).isSameAs(meter2);

        verify(listener, never()).onMeterAdded(MetricName.build("thing"), meter1);
    }

    @Test
    public void accessingACustomMeterRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Meter> supplier = () -> meter;
        final Meter meter1 = registry.meter(MetricName.build("thing"), supplier);
        final Meter meter2 = registry.meter(MetricName.build("thing"), supplier);

        assertThat(meter1).isExactlyInstanceOf(NoopMetricRegistry.NoopMeter.class);
        assertThat(meter2).isExactlyInstanceOf(NoopMetricRegistry.NoopMeter.class);
        assertThat(meter1).isSameAs(meter2);

        verify(listener, never()).onMeterAdded(MetricName.build("thing"), meter1);
    }

    @Test
    public void removingAMeterTriggersNoNotification() {
        registry.register(MetricName.build("thing"), meter);

        assertThat(registry.remove(MetricName.build("thing"))).isFalse();

        verify(listener, never()).onMeterRemoved(MetricName.build("thing"));
    }

    @Test
    public void registeringATimerTriggersNoNotification() {
        assertThat(registry.register(MetricName.build("thing"), timer)).isEqualTo(timer);

        verify(listener, never()).onTimerAdded(MetricName.build("thing"), timer);
    }

    @Test
    public void accessingATimerRegistersAndReusesIt() {
        final Timer timer1 = registry.timer(MetricName.build("thing"));
        final Timer timer2 = registry.timer(MetricName.build("thing"));

        assertThat(timer1).isExactlyInstanceOf(NoopMetricRegistry.NoopTimer.class);
        assertThat(timer2).isExactlyInstanceOf(NoopMetricRegistry.NoopTimer.class);
        assertThat(timer1).isSameAs(timer2);

        verify(listener, never()).onTimerAdded(MetricName.build("thing"), timer1);
    }

    @Test
    public void accessingACustomTimerRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Timer> supplier = () -> timer;
        final Timer timer1 = registry.timer(MetricName.build("thing"), supplier);
        final Timer timer2 = registry.timer(MetricName.build("thing"), supplier);

        assertThat(timer1).isExactlyInstanceOf(NoopMetricRegistry.NoopTimer.class);
        assertThat(timer2).isExactlyInstanceOf(NoopMetricRegistry.NoopTimer.class);
        assertThat(timer1).isSameAs(timer2);

        verify(listener, never()).onTimerAdded(MetricName.build("thing"), timer1);
    }


    @Test
    public void removingATimerTriggersNoNotification() {
        registry.register(MetricName.build("thing"), timer);

        assertThat(registry.remove(MetricName.build("thing"))).isFalse();

        verify(listener, never()).onTimerRemoved(MetricName.build("thing"));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void accessingACustomGaugeRegistersAndReusesIt() {
        final MetricRegistry.MetricSupplier<Gauge<String>> supplier = () -> gauge;
        final Gauge gauge1 = registry.gauge(MetricName.build("thing"), supplier);
        final Gauge gauge2 = registry.gauge(MetricName.build("thing"), supplier);

        assertThat(gauge1).isExactlyInstanceOf(NoopMetricRegistry.NoopGauge.class);
        assertThat(gauge2).isExactlyInstanceOf(NoopMetricRegistry.NoopGauge.class);
        assertThat(gauge1).isSameAs(gauge2);

        verify(listener, never()).onGaugeAdded(MetricName.build("thing"), gauge1);
    }


    @Test
    public void addingAListenerWithExistingMetricsDoesNotNotify() {
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
    public void aRemovedListenerDoesNotReceiveUpdates() {
        registry.register(MetricName.build("gauge"), gauge);
        registry.removeListener(listener);
        registry.register("gauge2", gauge);

        verify(listener, never()).onGaugeAdded(MetricName.build("gauge2"), gauge);
    }

    @Test
    public void hasAMapOfRegisteredGauges() {
        registry.register(MetricName.build("gauge"), gauge);

        assertThat(registry.getGauges()).isEmpty();
    }

    @Test
    public void hasAMapOfRegisteredCounters() {
        registry.register(MetricName.build("counter"), counter);

        assertThat(registry.getCounters()).isEmpty();
    }

    @Test
    public void hasAMapOfRegisteredHistograms() {
        registry.register(MetricName.build("histogram"), histogram);

        assertThat(registry.getHistograms()).isEmpty();
    }

    @Test
    public void hasAMapOfRegisteredMeters() {
        registry.register(MetricName.build("meter"), meter);

        assertThat(registry.getMeters()).isEmpty();
    }

    @Test
    public void hasAMapOfRegisteredTimers() {
        registry.register(MetricName.build("timer"), timer);

        assertThat(registry.getTimers()).isEmpty();
    }

    @Test
    public void hasASetOfRegisteredMetricNames() {
        registry.register(MetricName.build("gauge"), gauge);
        registry.register(MetricName.build("counter"), counter);
        registry.register(MetricName.build("histogram"), histogram);
        registry.register(MetricName.build("meter"), meter);
        registry.register(MetricName.build("timer"), timer);

        assertThat(registry.getNames()).isEmpty();
    }

    @Test
    public void doesNotRegisterMultipleMetrics() {
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
    public void doesNotRegisterMultipleMetricsWithAPrefix() {
        final MetricSet metrics = () -> {
            final Map<MetricName, Metric> m = new HashMap<>();
            m.put(MetricName.build("gauge"), gauge);
            m.put(MetricName.build("counter"), counter);
            return m;
        };

        registry.register("my", metrics);

        assertThat(registry.getNames()).isEmpty();
    }

    @Test
    public void doesNotRegisterRecursiveMetricSets() {
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

        registry.register("my", outer);

        assertThat(registry.getNames()).isEmpty();
    }

    @Test
    public void doesNotRegisterMetricsFromAnotherRegistry() {
        MetricRegistry other = new MetricRegistry();
        other.register(MetricName.build("gauge"), gauge);
        registry.register("nested", other);
        assertThat(registry.getNames()).isEmpty();
    }

    @Test
    public void removesMetricsMatchingAFilter() {
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
    public void addingChildMetricAfterRegister() {
        MetricRegistry parent = new NoopMetricRegistry();
        MetricRegistry child = new MetricRegistry();

        child.counter(MetricName.build("test-1"));
        parent.register(MetricName.build("child"), child);
        child.counter(MetricName.build("test-2"));

        assertThat(parent.getMetrics()).isEmpty();
    }

    @Test
    public void addingMultipleChildMetricsAfterRegister() {
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
    public void addingDeepChildMetricsAfterRegister() {
        MetricRegistry parent = new NoopMetricRegistry();
        MetricRegistry child = new MetricRegistry();
        MetricRegistry deepChild = new MetricRegistry();

        deepChild.counter(MetricName.build("test-1"));
        child.register("deep-child", deepChild);
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
    public void removingChildMetricAfterRegister() {
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
    public void removingMultipleChildMetricsAfterRegister() {
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
    public void removingDeepChildMetricsAfterRegister() {
        MetricRegistry parent = new NoopMetricRegistry();
        MetricRegistry child = new MetricRegistry();
        MetricRegistry deepChild = new MetricRegistry();

        deepChild.counter(MetricName.build("test-1"));
        child.register("deep-child", deepChild);
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
    public void registerNullMetric() {
        MetricRegistry registry = new NoopMetricRegistry();
        assertThatNullPointerException()
                .isThrownBy(() -> registry.register("any_name", null))
                .withMessage("metric == null");
    }
}
