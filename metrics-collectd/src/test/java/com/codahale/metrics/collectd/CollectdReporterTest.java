package com.codahale.metrics.collectd;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.collectd.api.ValueList;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CollectdReporterTest {

    @ClassRule
    public static Receiver receiver = new Receiver(25826);

    private final MetricRegistry registry = new MetricRegistry();
    private CollectdReporter reporter;

    @Before
    public void setUp() {
        reporter = CollectdReporter.forRegistry(registry)
                .withHostName("eddie")
                .build(new Sender("localhost", 25826));
    }

    @Test
    public void reportsByteGauges() throws Exception {
        reportsGauges((byte) 128);
    }

    @Test
    public void reportsShortGauges() throws Exception {
        reportsGauges((short) 2048);
    }

    @Test
    public void reportsIntegerGauges() throws Exception {
        reportsGauges(42);
    }

    @Test
    public void reportsLongGauges() throws Exception {
        reportsGauges(Long.MAX_VALUE);
    }

    @Test
    public void reportsFloatGauges() throws Exception {
        reportsGauges(0.25);
    }

    @Test
    public void reportsDoubleGauges() throws Exception {
        reportsGauges(0.125d);
    }

    private <T extends Number> void reportsGauges(T value) throws Exception {
        reporter.report(
                map("gauge", (Gauge) () -> value),
                map(),
                map(),
                map(),
                map());

        ValueList data = receiver.next();
        assertThat(data.getValues()).containsExactly(value.doubleValue());
    }

    @Test
    public void reportsBooleanGauges() throws Exception {
        reporter.report(
                map("gauge", (Gauge) () -> true),
                map(),
                map(),
                map(),
                map());

        ValueList data = receiver.next();
        assertThat(data.getValues()).containsExactly(1d);

        reporter.report(
                map("gauge", (Gauge) () -> false),
                map(),
                map(),
                map(),
                map());

        data = receiver.next();
        assertThat(data.getValues()).containsExactly(0d);
    }

    @Test
    public void doesNotReportStringGauges() throws Exception {
        reporter.report(
                map("unsupported", (Gauge) () -> "value"),
                map(),
                map(),
                map(),
                map());

        assertThat(receiver.next()).isNull();
    }

    @Test
    public void reportsCounters() throws Exception {
        Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(42L);

        reporter.report(
                map(),
                map("api.rest.requests.count", counter),
                map(),
                map(),
                map());

        ValueList data = receiver.next();
        assertThat(data.getValues()).containsExactly(42d);
    }

    @Test
    public void reportsMeters() throws Exception {
        Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getOneMinuteRate()).thenReturn(2.0);
        when(meter.getFiveMinuteRate()).thenReturn(3.0);
        when(meter.getFifteenMinuteRate()).thenReturn(4.0);
        when(meter.getMeanRate()).thenReturn(5.0);

        reporter.report(
                map(),
                map(),
                map(),
                map("api.rest.requests", meter),
                map());

        assertThat(receiver.next().getValues()).containsExactly(1d);
        assertThat(receiver.next().getValues()).containsExactly(2d);
        assertThat(receiver.next().getValues()).containsExactly(3d);
        assertThat(receiver.next().getValues()).containsExactly(4d);
        assertThat(receiver.next().getValues()).containsExactly(5d);
    }

    @Test
    public void reportsHistograms() throws Exception {
        Histogram histogram = mock(Histogram.class);
        Snapshot snapshot = mock(Snapshot.class);
        when(histogram.getCount()).thenReturn(1L);
        when(histogram.getSnapshot()).thenReturn(snapshot);
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

        reporter.report(
                map(),
                map(),
                map("histogram", histogram),
                map(),
                map());

        for (int i = 1; i <= 11; i++) {
            assertThat(receiver.next().getValues()).containsExactly((double) i);
        }
    }

    @Test
    public void reportsTimers() throws Exception {
        Timer timer = mock(Timer.class);
        Snapshot snapshot = mock(Snapshot.class);
        when(timer.getSnapshot()).thenReturn(snapshot);
        when(timer.getCount()).thenReturn(1L);
        when(timer.getSnapshot()).thenReturn(snapshot);
        when(snapshot.getMax()).thenReturn(MILLISECONDS.toNanos(100));
        when(snapshot.getMean()).thenReturn((double) MILLISECONDS.toNanos(200));
        when(snapshot.getMin()).thenReturn(MILLISECONDS.toNanos(300));
        when(snapshot.getStdDev()).thenReturn((double) MILLISECONDS.toNanos(400));
        when(snapshot.getMedian()).thenReturn((double) MILLISECONDS.toNanos(500));
        when(snapshot.get75thPercentile()).thenReturn((double) MILLISECONDS.toNanos(600));
        when(snapshot.get95thPercentile()).thenReturn((double) MILLISECONDS.toNanos(700));
        when(snapshot.get98thPercentile()).thenReturn((double) MILLISECONDS.toNanos(800));
        when(snapshot.get99thPercentile()).thenReturn((double) MILLISECONDS.toNanos(900));
        when(snapshot.get999thPercentile()).thenReturn((double) MILLISECONDS.toNanos(1000));
        when(timer.getOneMinuteRate()).thenReturn(11.0);
        when(timer.getFiveMinuteRate()).thenReturn(12.0);
        when(timer.getFifteenMinuteRate()).thenReturn(13.0);
        when(timer.getMeanRate()).thenReturn(14.0);

        reporter.report(
                map(),
                map(),
                map(),
                map(),
                map("timer", timer));

        assertThat(receiver.next().getValues()).containsExactly(1d);
        assertThat(receiver.next().getValues()).containsExactly(100d);
        assertThat(receiver.next().getValues()).containsExactly(200d);
        assertThat(receiver.next().getValues()).containsExactly(300d);
        assertThat(receiver.next().getValues()).containsExactly(400d);
        assertThat(receiver.next().getValues()).containsExactly(500d);
        assertThat(receiver.next().getValues()).containsExactly(600d);
        assertThat(receiver.next().getValues()).containsExactly(700d);
        assertThat(receiver.next().getValues()).containsExactly(800d);
        assertThat(receiver.next().getValues()).containsExactly(900d);
        assertThat(receiver.next().getValues()).containsExactly(1000d);
        assertThat(receiver.next().getValues()).containsExactly(11d);
        assertThat(receiver.next().getValues()).containsExactly(12d);
        assertThat(receiver.next().getValues()).containsExactly(13d);
        assertThat(receiver.next().getValues()).containsExactly(14d);
    }

    @Test
    public void sanitizesMetricName() throws Exception {
        Counter counter = registry.counter("dash-illegal.slash/illegal");
        counter.inc();

        reporter.report();

        ValueList values = receiver.next();
        assertThat(values.getPlugin()).isEqualTo("dash_illegal.slash_illegal");
    }

    @Test
    public void testUnableSetSecurityLevelToSignWithoutUsername() {
        assertThatIllegalArgumentException().isThrownBy(()->
                CollectdReporter.forRegistry(registry)
                        .withHostName("eddie")
                        .withSecurityLevel(SecurityLevel.SIGN)
                        .withPassword("t1_g3r")
                        .build(new Sender("localhost", 25826)))
                .withMessage("username is required for securityLevel: SIGN");
    }

    @Test
    public void testUnableSetSecurityLevelToSignWithoutPassword() {
        assertThatIllegalArgumentException().isThrownBy(()->
                CollectdReporter.forRegistry(registry)
                        .withHostName("eddie")
                        .withSecurityLevel(SecurityLevel.SIGN)
                        .withUsername("scott")
                        .build(new Sender("localhost", 25826)))
                .withMessage("password is required for securityLevel: SIGN");
    }

    private <T> SortedMap<String, T> map() {
        return new TreeMap<>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final SortedMap<String, T> map = map();
        map.put(name, metric);
        return map;
    }

}


