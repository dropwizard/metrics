package com.codahale.metrics.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.codahale.metrics.*;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsModuleTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(
            new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, false));

    @Test
    public void serializesGauges() throws Exception {
        final Gauge<Integer> gauge = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 100;
            }
        };

        assertThat(mapper.writeValueAsString(gauge))
                .isEqualTo("{\"value\":100}");
    }

    @Test
    public void serializesGaugesThatThrowExceptions() throws Exception {
        final Gauge<Integer> gauge = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                throw new IllegalArgumentException("poops");
            }
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
        when(snapshot.getValues()).thenReturn(new long[]{ 1, 2, 3 });

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
                new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, true));

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
                new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, true));

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
                                   "\"version\":\"3.0.0\"," +
                                   "\"gauges\":{}," +
                                   "\"counters\":{}," +
                                   "\"histograms\":{}," +
                                   "\"meters\":{}," +
                                   "\"timers\":{}}");
    }
}
