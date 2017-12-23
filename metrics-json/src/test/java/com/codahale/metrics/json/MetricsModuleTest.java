package com.codahale.metrics.json;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsModuleTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(
            new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, false, MetricFilter.ALL));
    private static final int MS_TO_NS = 1_000_000;

    @Test
    public void serializesGauges() throws Exception {
        final Gauge<Integer> gauge = () -> 100;

        assertThat(mapper.writeValueAsString(gauge))
                .isEqualTo("{\"value\":100}");
    }

    @Test
    public void serializesGaugesThatThrowExceptions() throws Exception {
        final Gauge<Integer> gauge = () -> {
            throw new IllegalArgumentException("poops");
        };

        assertThat(mapper.writeValueAsString(gauge))
                .isEqualTo("{\"error\":\"java.lang.IllegalArgumentException: poops\"}");
    }

    @Test
    public void serializesCounters() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        assertThat(mapper.writeValueAsString(counter))
                .isEqualTo("{\"count\":100}");
    }

    @Test
    public void serializesHistograms() throws Exception {
        final Histogram histogram = mock(Histogram.class);
        when(histogram.getCount()).thenReturn(1L);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(2L);
        when(snapshot.getMean()).thenReturn(3.0);
        when(snapshot.getMin()).thenReturn(4L);
        when(snapshot.getStdDev()).thenReturn(5.0);
        when(snapshot.getMedian()).thenReturn(6.0);
        when(snapshot.get75thPercentile()).thenReturn(7.0);
        when(snapshot.get95thPercentile()).thenReturn(8.0);
        when(snapshot.get98thPercentile()).thenReturn(9.0);
        when(snapshot.get99thPercentile()).thenReturn(10.0);
        when(snapshot.get999thPercentile()).thenReturn(11.0);
        when(snapshot.getValues()).thenReturn(new long[]{1, 2, 3});

        when(histogram.getSnapshot()).thenReturn(snapshot);

        assertThat(mapper.writeValueAsString(histogram))
                .isEqualTo("{" +
                        "\"count\":1," +
                        "\"max\":2," +
                        "\"mean\":3.0," +
                        "\"min\":4," +
                        "\"p50\":6.0," +
                        "\"p75\":7.0," +
                        "\"p95\":8.0," +
                        "\"p98\":9.0," +
                        "\"p99\":10.0," +
                        "\"p999\":11.0," +
                        "\"stddev\":5.0}");

        final ObjectMapper fullMapper = new ObjectMapper().registerModule(
                new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, true, MetricFilter.ALL));

        assertThat(fullMapper.writeValueAsString(histogram))
                .isEqualTo("{" +
                        "\"count\":1," +
                        "\"max\":2," +
                        "\"mean\":3.0," +
                        "\"min\":4," +
                        "\"p50\":6.0," +
                        "\"p75\":7.0," +
                        "\"p95\":8.0," +
                        "\"p98\":9.0," +
                        "\"p99\":10.0," +
                        "\"p999\":11.0," +
                        "\"values\":[1,2,3]," +
                        "\"stddev\":5.0}");
    }

    @Test
    public void serializesMeters() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(5.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(3.0);

        assertThat(mapper.writeValueAsString(meter))
                .isEqualTo("{" +
                        "\"count\":1," +
                        "\"m15_rate\":3.0," +
                        "\"m1_rate\":5.0," +
                        "\"m5_rate\":4.0," +
                        "\"mean_rate\":2.0," +
                        "\"units\":\"events/second\"}");
    }

    @Test
    public void serializesTimers() throws Exception {
        final Timer timer = mock(Timer.class);
        when(timer.getCount()).thenReturn(1L);
        when(timer.getMeanRate()).thenReturn(2.0);
        when(timer.getOneMinuteRate()).thenReturn(3.0);
        when(timer.getFiveMinuteRate()).thenReturn(4.0);
        when(timer.getFifteenMinuteRate()).thenReturn(5.0);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
        when(snapshot.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
        when(snapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
        when(snapshot.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));
        when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(1000));

        when(snapshot.getValues()).thenReturn(new long[]{
                TimeUnit.MILLISECONDS.toNanos(1),
                TimeUnit.MILLISECONDS.toNanos(2),
                TimeUnit.MILLISECONDS.toNanos(3)
        });

        when(timer.getSnapshot()).thenReturn(snapshot);

        assertThat(mapper.writeValueAsString(timer))
                .isEqualTo("{" +
                        "\"count\":1," +
                        "\"max\":100.0," +
                        "\"mean\":200.0," +
                        "\"min\":300.0," +
                        "\"p50\":500.0," +
                        "\"p75\":600.0," +
                        "\"p95\":700.0," +
                        "\"p98\":800.0," +
                        "\"p99\":900.0," +
                        "\"p999\":1000.0," +
                        "\"stddev\":400.0," +
                        "\"m15_rate\":5.0," +
                        "\"m1_rate\":3.0," +
                        "\"m5_rate\":4.0," +
                        "\"mean_rate\":2.0," +
                        "\"duration_units\":\"milliseconds\"," +
                        "\"rate_units\":\"calls/second\"}");

        final ObjectMapper fullMapper = new ObjectMapper().registerModule(
                new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, true, MetricFilter.ALL));

        assertThat(fullMapper.writeValueAsString(timer))
                .isEqualTo("{" +
                        "\"count\":1," +
                        "\"max\":100.0," +
                        "\"mean\":200.0," +
                        "\"min\":300.0," +
                        "\"p50\":500.0," +
                        "\"p75\":600.0," +
                        "\"p95\":700.0," +
                        "\"p98\":800.0," +
                        "\"p99\":900.0," +
                        "\"p999\":1000.0," +
                        "\"values\":[1.0,2.0,3.0]," +
                        "\"stddev\":400.0," +
                        "\"m15_rate\":5.0," +
                        "\"m1_rate\":3.0," +
                        "\"m5_rate\":4.0," +
                        "\"mean_rate\":2.0," +
                        "\"duration_units\":\"milliseconds\"," +
                        "\"rate_units\":\"calls/second\"}");
    }

    @Test
    public void serializesMetricRegistries() throws Exception {
        final MetricRegistry registry = new MetricRegistry();

        assertThat(mapper.writeValueAsString(registry))
                .isEqualTo("{" +
                        "\"version\":\"4.0.0\"," +
                        "\"gauges\":{}," +
                        "\"counters\":{}," +
                        "\"histograms\":{}," +
                        "\"meters\":{}," +
                        "\"timers\":{}}");
    }

    @Test
    public void unableCreateNegativeScale() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, false,
                        MetricFilter.ALL, Optional.of(-1))))
                .withMessage("scale should be >= 0");
    }

    @Test
    public void serializesGaugeWithScale() throws Exception {
        final Gauge<Double> gauge = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(1, 3);
            }
        };

        assertThat(new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, false,
                MetricFilter.ALL, Optional.of(2))).writeValueAsString(gauge)).isEqualTo("{\"value\":0.33}");
    }

    @Test
    public void serializesHistogramsWithScale() throws Exception {
        final Histogram histogram = mock(Histogram.class);
        when(histogram.getCount()).thenReturn(1L);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(2L);
        when(snapshot.getMean()).thenReturn(3.333333);
        when(snapshot.getMin()).thenReturn(4L);
        when(snapshot.getStdDev()).thenReturn(5.55555);
        when(snapshot.getMedian()).thenReturn(6.66666);
        when(snapshot.get75thPercentile()).thenReturn(7.77777);
        when(snapshot.get95thPercentile()).thenReturn(8.88888);
        when(snapshot.get98thPercentile()).thenReturn(9.99999);
        when(snapshot.get99thPercentile()).thenReturn(10.00001);
        when(snapshot.get999thPercentile()).thenReturn(11.11111);
        when(histogram.getSnapshot()).thenReturn(snapshot);

        assertThat(new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS,
                false, MetricFilter.ALL, Optional.of(2))).writeValueAsString(histogram)).isEqualTo("{" +
                        "\"count\":1," +
                        "\"max\":2," +
                        "\"mean\":3.33," +
                        "\"min\":4," +
                        "\"p50\":6.67," +
                        "\"p75\":7.78," +
                        "\"p95\":8.89," +
                        "\"p98\":10.0," +
                        "\"p99\":10.0," +
                        "\"p999\":11.11," +
                        "\"stddev\":5.56}");
    }

    @Test
    public void serializesMetersWithPrecision() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.22222);
        when(meter.getOneMinuteRate()).thenReturn(5.55555);
        when(meter.getFiveMinuteRate()).thenReturn(4.00001);
        when(meter.getFifteenMinuteRate()).thenReturn(3.33333);

        assertThat(new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS,
                false, MetricFilter.ALL, Optional.of(2))).writeValueAsString(meter))
                .isEqualTo("{" +
                        "\"count\":1," +
                        "\"m15_rate\":3.33," +
                        "\"m1_rate\":5.56," +
                        "\"m5_rate\":4.0," +
                        "\"mean_rate\":2.22," +
                        "\"units\":\"events/second\"}");
    }

    @Test
    public void serializesTimersWithPrecision() throws Exception {
        final Timer timer = mock(Timer.class);
        when(timer.getCount()).thenReturn(1L);
        when(timer.getMeanRate()).thenReturn(2.0);
        when(timer.getOneMinuteRate()).thenReturn(3.0);
        when(timer.getFiveMinuteRate()).thenReturn(4.0);
        when(timer.getFifteenMinuteRate()).thenReturn(5.0);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
        when(snapshot.getMean()).thenReturn(200.22222 * MS_TO_NS);
        when(snapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
        when(snapshot.getStdDev()).thenReturn(400.44444 * MS_TO_NS);
        when(snapshot.getMedian()).thenReturn(500.55555 * MS_TO_NS);
        when(snapshot.get75thPercentile()).thenReturn(600.66666 * MS_TO_NS);
        when(snapshot.get95thPercentile()).thenReturn(700.77777 * MS_TO_NS);
        when(snapshot.get98thPercentile()).thenReturn(800.88888 * MS_TO_NS);
        when(snapshot.get99thPercentile()).thenReturn(900.99999 * MS_TO_NS);
        when(snapshot.get999thPercentile()).thenReturn(1000.00001 * MS_TO_NS);
        when(timer.getSnapshot()).thenReturn(snapshot);

        assertThat(new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS,
                false, MetricFilter.ALL, Optional.of(2))).writeValueAsString(timer)).isEqualTo("{" +
                        "\"count\":1," +
                        "\"max\":100.0," +
                        "\"mean\":200.22," +
                        "\"min\":300.0," +
                        "\"p50\":500.56," +
                        "\"p75\":600.67," +
                        "\"p95\":700.78," +
                        "\"p98\":800.89," +
                        "\"p99\":901.0," +
                        "\"p999\":1000.0," +
                        "\"stddev\":400.44," +
                        "\"m15_rate\":5.0," +
                        "\"m1_rate\":3.0," +
                        "\"m5_rate\":4.0," +
                        "\"mean_rate\":2.0," +
                        "\"duration_units\":\"milliseconds\"," +
                        "\"rate_units\":\"calls/second\"}");
    }

    @Test
    public void serializesMetersWithZeroPrecision() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.22222);
        when(meter.getOneMinuteRate()).thenReturn(5.55555);
        when(meter.getFiveMinuteRate()).thenReturn(4.00001);
        when(meter.getFifteenMinuteRate()).thenReturn(3.33333);

        assertThat(new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS,
                false, MetricFilter.ALL, Optional.of(0))).writeValueAsString(meter))
                .isEqualTo("{" +
                        "\"count\":1," +
                        "\"m15_rate\":3.0," +
                        "\"m1_rate\":6.0," +
                        "\"m5_rate\":4.0," +
                        "\"mean_rate\":2.0," +
                        "\"units\":\"events/second\"}");
    }
}
