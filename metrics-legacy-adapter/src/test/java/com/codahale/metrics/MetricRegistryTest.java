package com.codahale.metrics;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@SuppressWarnings("deprecation")
public class MetricRegistryTest {

    private MetricRegistry metricRegistry = new MetricRegistry();

    @Test
    public void testRegisterMetric() {
        Counter counter = metricRegistry.register("test-counter", new Counter());
        counter.inc(42);
        assertThat(metricRegistry.counter("test-counter").getCount()).isEqualTo(42);
    }

    @Test
    public void testRegisterAll() {
        metricRegistry.registerAll(() -> {
            Map<String, Metric> map = new HashMap<>();
            map.put("test-counter", new Counter());
            map.put("test-gauge", (Gauge<Integer>) () -> 28);
            map.put("test-histogram", new Histogram(new ExponentiallyDecayingReservoir()));
            return map;
        });
        Map<String, Metric> metrics = metricRegistry.getMetrics();
        assertThat(metrics).containsOnlyKeys("test-counter", "test-histogram", "test-gauge");
        assertThat(metrics.get("test-counter")).isInstanceOf(Counter.class);
        assertThat(metrics.get("test-histogram")).isInstanceOf(Histogram.class);
        assertThat(metrics.get("test-gauge")).isInstanceOf(Gauge.class);
    }

    @Test
    public void testCreateCustomGauge() {
        Gauge gauge = metricRegistry.gauge("test-gauge-supplier", () -> () -> 42);
        assertThat(gauge.getValue()).isEqualTo(42);
    }

    @Test
    public void testCreateCounter() {
        Counter counter = metricRegistry.counter("test-counter");
        counter.inc(42);
        assertThat(metricRegistry.counter("test-counter").getCount()).isEqualTo(42);
    }

    @Test
    public void testCreateCustomCounter() {
        Counter counter = metricRegistry.counter("test-custom-counter", () -> {
            Counter c = new Counter();
            c.inc(8);
            return c;
        });
        counter.inc(16);
        assertThat(metricRegistry.counter("test-custom-counter").getCount()).isEqualTo(24);
    }

    @Test
    public void testCreateHistogram() {
        Histogram histogram = metricRegistry.histogram("test-histogram");
        histogram.update(100);
        histogram.update(200);
        histogram.update(180);
        assertThat(metricRegistry.histogram("test-histogram").getSnapshot().getMean())
                .isCloseTo(160.0, offset(0.1));
    }

    @Test
    public void testCreateCustomHistogram() {
        Histogram histogram = metricRegistry.histogram("test-custom-histogram",
                () -> new Histogram(new SlidingWindowReservoir(2)));
        histogram.update(100);
        histogram.update(200);
        histogram.update(180);
        assertThat(metricRegistry.histogram("test-custom-histogram").getSnapshot().getMean())
                .isCloseTo(190.0, offset(0.1));
    }

    @Test
    public void testCreateMeter() {
        Meter meter = metricRegistry.meter("test-meter");
        meter.mark();
        meter.mark(2);

        assertThat(metricRegistry.meter("test-meter").getCount()).isEqualTo(3);
    }

    @Test
    public void testCreateCustomMeter() {
        Meter meter = metricRegistry.meter("test-custom-meter", () -> {
            Meter m = new Meter();
            m.mark(16);
            return m;
        });
        meter.mark();

        assertThat(metricRegistry.meter("test-custom-meter").getCount()).isEqualTo(17);
    }

    @Test
    public void testCreateTimer() {
        Timer timer = metricRegistry.timer("test-timer");
        timer.update(100, TimeUnit.MILLISECONDS);
        timer.update(200, TimeUnit.MILLISECONDS);
        timer.update(180, TimeUnit.MILLISECONDS);

        assertThat(metricRegistry.timer("test-timer").getCount()).isEqualTo(3);
    }

    @Test
    public void testCreateCustomTimer() {
        Timer timer = metricRegistry.timer("custom-test-timer", () -> {
            Timer t = new Timer(new UniformReservoir());
            t.update(300, TimeUnit.MILLISECONDS);
            t.update(200, TimeUnit.MILLISECONDS);
            return t;
        });
        timer.update(180, TimeUnit.MILLISECONDS);

        assertThat(metricRegistry.timer("custom-test-timer").getCount()).isEqualTo(3);
    }

    @Test
    public void testRemoveMetric() {
        metricRegistry.timer("test-timer");
        metricRegistry.counter("test-counter");
        metricRegistry.meter("test-meter");

        assertThat(metricRegistry.remove("test-counter")).isTrue();

        assertThat(metricRegistry.getMetrics()).containsOnlyKeys("test-timer", "test-meter");
    }

    @Test
    public void testRemoveMatching() {
        metricRegistry.counter("test-counter");
        metricRegistry.timer("test-timer");
        metricRegistry.timer("test-custom-timer");
        metricRegistry.meter("test-meter");

        metricRegistry.removeMatching((name, metric) -> metric instanceof Timer && name.startsWith("test"));
        assertThat(metricRegistry.getMetrics()).containsOnlyKeys("test-counter", "test-meter");
    }

    @Test
    public void testAddListenerForGauge() throws Exception {
        CountDownLatch gaugeAddedLatch = new CountDownLatch(1);
        CountDownLatch gaugeRemovedLatch = new CountDownLatch(1);
        metricRegistry.addListener(new MetricRegistryListener.Base() {
            @Override
            public void onGaugeAdded(String name, Gauge<?> gauge) {
                assertThat(name).isEqualTo("test-gauge");
                assertThat(gauge.getValue()).isEqualTo(42);
                gaugeAddedLatch.countDown();
            }

            @Override
            public void onGaugeRemoved(String name) {
                assertThat(name).isEqualTo("test-gauge");
                gaugeRemovedLatch.countDown();
            }
        });

        metricRegistry.register("test-gauge", (Gauge<Integer>) () -> 42);
        gaugeAddedLatch.await(5, TimeUnit.SECONDS);
        assertThat(gaugeAddedLatch.getCount()).isEqualTo(0);

        metricRegistry.remove("test-gauge");
        gaugeRemovedLatch.await(5, TimeUnit.SECONDS);
        assertThat(gaugeRemovedLatch.getCount()).isEqualTo(0);
    }

    @Test
    public void testAddListenerForCounter() throws Exception {
        CountDownLatch counterAddedLatch = new CountDownLatch(1);
        CountDownLatch counterRemovedLatch = new CountDownLatch(1);
        metricRegistry.addListener(new MetricRegistryListener.Base() {
            @Override
            public void onCounterAdded(String name, Counter counter) {
                assertThat(name).isEqualTo("test-counter");
                counterAddedLatch.countDown();
            }

            @Override
            public void onCounterRemoved(String name) {
                assertThat(name).isEqualTo("test-counter");
                counterRemovedLatch.countDown();
            }
        });

        metricRegistry.counter("test-counter");
        counterAddedLatch.await(5, TimeUnit.SECONDS);
        assertThat(counterAddedLatch.getCount()).isEqualTo(0);

        metricRegistry.remove("test-counter");
        counterRemovedLatch.await(5, TimeUnit.SECONDS);
        assertThat(counterRemovedLatch.getCount()).isEqualTo(0);
    }

    @Test
    public void testAddListenerForHistogram() throws Exception {
        CountDownLatch histogramAddedLatch = new CountDownLatch(1);
        CountDownLatch histogramRemovedLatch = new CountDownLatch(1);
        metricRegistry.addListener(new MetricRegistryListener.Base() {


            @Override
            public void onHistogramAdded(String name, Histogram histogram) {
                assertThat(name).isEqualTo("test-histogram");
                histogramAddedLatch.countDown();
            }

            @Override
            public void onHistogramRemoved(String name) {
                assertThat(name).isEqualTo("test-histogram");
                histogramRemovedLatch.countDown();
            }
        });

        metricRegistry.histogram("test-histogram");
        histogramAddedLatch.await(5, TimeUnit.SECONDS);
        assertThat(histogramAddedLatch.getCount()).isEqualTo(0);

        metricRegistry.remove("test-histogram");
        histogramRemovedLatch.await(5, TimeUnit.SECONDS);
        assertThat(histogramRemovedLatch.getCount()).isEqualTo(0);
    }

    @Test
    public void testAddListenerForMeter() throws Exception {
        CountDownLatch meterAddedLatch = new CountDownLatch(1);
        CountDownLatch meterRemovedLatch = new CountDownLatch(1);
        metricRegistry.addListener(new MetricRegistryListener.Base() {

            @Override
            public void onMeterAdded(String name, Meter meter) {
                assertThat(name).isEqualTo("test-meter");
                meterAddedLatch.countDown();
            }

            @Override
            public void onMeterRemoved(String name) {
                assertThat(name).isEqualTo("test-meter");
                meterRemovedLatch.countDown();
            }
        });

        metricRegistry.meter("test-meter");
        meterAddedLatch.await(5, TimeUnit.SECONDS);
        assertThat(meterAddedLatch.getCount()).isEqualTo(0);

        metricRegistry.remove("test-meter");
        meterRemovedLatch.await(5, TimeUnit.SECONDS);
        assertThat(meterRemovedLatch.getCount()).isEqualTo(0);
    }

    @Test
    public void testAddListenerForTimer() throws Exception {
        CountDownLatch timerAddedLatch = new CountDownLatch(1);
        CountDownLatch timerRemovedLatch = new CountDownLatch(1);
        metricRegistry.addListener(new MetricRegistryListener.Base() {

            @Override
            public void onTimerAdded(String name, Timer timer) {
                assertThat(name).isEqualTo("test-timer");
                timerAddedLatch.countDown();
            }

            @Override
            public void onTimerRemoved(String name) {
                assertThat(name).isEqualTo("test-timer");
                timerRemovedLatch.countDown();
            }
        });

        metricRegistry.timer("test-timer");
        timerAddedLatch.await(5, TimeUnit.SECONDS);
        assertThat(timerAddedLatch.getCount()).isEqualTo(0);

        metricRegistry.remove("test-timer");
        timerRemovedLatch.await(5, TimeUnit.SECONDS);
        assertThat(timerRemovedLatch.getCount()).isEqualTo(0);
    }

    @Test
    public void testRemoveListener() throws Exception {
        CountDownLatch gaugeAddedLatch = new CountDownLatch(1);
        MetricRegistryListener listener = new MetricRegistryListener.Base() {
            @Override
            public void onGaugeAdded(String name, Gauge<?> gauge) {
                gaugeAddedLatch.countDown();
            }
        };
        metricRegistry.addListener(listener);
        metricRegistry.removeListener(listener);

        metricRegistry.register("test-gauge", (Gauge<Integer>) () -> 39);

        gaugeAddedLatch.await(100, TimeUnit.MILLISECONDS);
        assertThat(gaugeAddedLatch.getCount()).isEqualTo(1);
    }

    @Test
    public void testGetNames() {
        metricRegistry.counter("test-counter");
        metricRegistry.timer("test-timer");
        metricRegistry.timer("test-custom-timer");
        metricRegistry.meter("test-meter");

        assertThat(metricRegistry.getNames()).containsExactly("test-counter", "test-custom-timer",
                "test-meter", "test-timer");
    }

    @Test
    public void testGetGauges() {
        metricRegistry.counter("test-counter");
        metricRegistry.timer("test-timer");
        metricRegistry.meter("test-meter");
        metricRegistry.register("test-text-gauge-2", new CachedGauge<String>(1, TimeUnit.MINUTES) {
            @Override
            protected String loadValue() {
                return "eu2";
            }
        });
        metricRegistry.register("test-gauge", (Gauge<Integer>) () -> 42);
        metricRegistry.register("test-text-gauge-1", new DerivativeGauge<Integer, String>(() -> 1) {
            @Override
            protected String transform(Integer value) {
                return "eu" + value;
            }
        });

        SortedMap<String, Gauge> gauges = metricRegistry.getGauges();
        assertThat(gauges).containsOnlyKeys("test-gauge", "test-text-gauge-1", "test-text-gauge-2");
    }

    @Test
    public void testGetGaugesWithFilter() {
        metricRegistry.counter("test-counter");
        metricRegistry.timer("test-timer");
        metricRegistry.meter("test-meter");
        metricRegistry.register("test-text-gauge-2", new CachedGauge<String>(1, TimeUnit.MINUTES) {
            @Override
            protected String loadValue() {
                return "eu2";
            }
        });
        metricRegistry.register("test-gauge", (Gauge<Integer>) () -> 42);
        metricRegistry.register("test-text-gauge-1", new DerivativeGauge<Integer, String>(() -> 1) {
            @Override
            protected String transform(Integer value) {
                return "eu" + value;
            }
        });

        assertThat(metricRegistry.getGauges((name, metric) -> name.contains("gauge") && metric instanceof CachedGauge))
                .containsOnlyKeys("test-text-gauge-2");
    }

    @Test
    public void testGetHistograms() {
        metricRegistry.counter("test-counter");
        metricRegistry.timer("test-timer");
        metricRegistry.meter("test-meter");
        metricRegistry.histogram("test-histogram-1");
        metricRegistry.histogram("test-histogram-3");
        metricRegistry.histogram("test-histogram-2");

        assertThat(metricRegistry.getHistograms())
                .containsOnlyKeys("test-histogram-1", "test-histogram-2", "test-histogram-3");
    }

    @Test
    public void testGetHistogramsWithFilter() {
        metricRegistry.counter("sw-counter");
        metricRegistry.timer("sw-timer");
        metricRegistry.meter("sw-meter");
        metricRegistry.histogram("sw-histogram-1");
        metricRegistry.histogram("se-histogram-3");
        metricRegistry.histogram("sw-histogram-2");

        assertThat(metricRegistry.getHistograms(MetricFilter.startsWith("sw")))
                .containsOnlyKeys("sw-histogram-1", "sw-histogram-2");
    }

    @Test
    public void testGetCounters() {
        metricRegistry.histogram("test-histogram");
        metricRegistry.timer("test-timer");
        metricRegistry.meter("test-meter");
        metricRegistry.counter("test-counter-1");
        metricRegistry.counter("test-counter-3");
        metricRegistry.counter("test-counter-2");

        assertThat(metricRegistry.getCounters())
                .containsOnlyKeys("test-counter-1", "test-counter-2", "test-counter-3");
    }

    @Test
    public void testGetCountersWithFilter() {
        metricRegistry.histogram("test-histogram");
        metricRegistry.timer("test-timer");
        metricRegistry.meter("test-meter");
        metricRegistry.counter("test-counter-1");
        metricRegistry.counter("test-counter-3");
        metricRegistry.counter("test-cnt-2");

        assertThat(metricRegistry.getCounters(MetricFilter.contains("counter")))
                .containsOnlyKeys("test-counter-1", "test-counter-3");
    }

    @Test
    public void testGetMeters() {
        metricRegistry.register("test-gauge", (Gauge<Integer>) () -> 42);
        metricRegistry.histogram("test-histogram");
        metricRegistry.timer("test-timer");
        metricRegistry.counter("test-counter");
        metricRegistry.meter("test-meter-1");
        metricRegistry.meter("test-meter-3");
        metricRegistry.meter("test-meter-2");

        assertThat(metricRegistry.getMeters()).containsOnlyKeys("test-meter-1", "test-meter-2", "test-meter-3");
    }

    @Test
    public void testGetMetersWithFilter() {
        metricRegistry.register("sw-gauge", (Gauge<Integer>) () -> 42);
        metricRegistry.histogram("sw-histogram");
        metricRegistry.timer("sw-timer");
        metricRegistry.counter("sw-counter");
        metricRegistry.meter("nw-meter-1");
        metricRegistry.meter("sw-meter-3");
        metricRegistry.meter("nw-meter-2");

        assertThat(metricRegistry.getMeters(MetricFilter.startsWith("sw"))).containsOnlyKeys("sw-meter-3");
    }

    @Test
    public void testGetTimers() {
        metricRegistry.histogram("test-histogram");
        metricRegistry.meter("test-meter");
        metricRegistry.counter("test-counter");
        metricRegistry.timer("test-timer-1");
        metricRegistry.timer("test-timer-3");
        metricRegistry.timer("test-timer-2");

        assertThat(metricRegistry.getTimers()).containsOnlyKeys("test-timer-1", "test-timer-2", "test-timer-3");
    }

    @Test
    public void testGetTimersWithFilter() {
        metricRegistry.histogram("test-histogram-2");
        metricRegistry.meter("test-meter-2");
        metricRegistry.counter("test-counter-2");
        metricRegistry.timer("test-timer-1");
        metricRegistry.timer("test-timer-3");
        metricRegistry.timer("test-timer-2");

        assertThat(metricRegistry.getTimers(MetricFilter.endsWith("2"))).containsOnlyKeys("test-timer-2");
    }

    @Test
    public void testGetMetrics() {
        metricRegistry.register("test-text-gauge-2", new CachedGauge<String>(1, TimeUnit.MINUTES) {
            @Override
            protected String loadValue() {
                return "eu2";
            }
        });
        metricRegistry.register("test-gauge", (Gauge<Integer>) () -> 42);
        metricRegistry.register("test-text-gauge-1", new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(1, 2);
            }
        });
        metricRegistry.histogram("test-histogram-1");
        metricRegistry.histogram("test-histogram-2");
        metricRegistry.meter("test-meter-1");
        metricRegistry.meter("test-meter-2");
        metricRegistry.counter("test-counter");
        metricRegistry.timer("test-timer-1");
        metricRegistry.timer("test-timer-2");
        MetricRegistry subMetrics = new MetricRegistry();
        subMetrics.counter("sb-counter-1");
        subMetrics.counter("sb-counter-2");
        subMetrics.histogram("sb-histogram-1");
        metricRegistry.register("test-ms", subMetrics);

        assertThat(metricRegistry.getMetrics()).containsOnlyKeys("test-text-gauge-2", "test-gauge", "test-text-gauge-1",
                "test-histogram-1", "test-histogram-2", "test-meter-1", "test-meter-2", "test-counter",
                "test-timer-1", "test-timer-2",
                "test-ms.sb-counter-1", "test-ms.sb-counter-2", "test-ms.sb-histogram-1");
    }
}
