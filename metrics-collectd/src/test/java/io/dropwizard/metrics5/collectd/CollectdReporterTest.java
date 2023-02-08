package io.dropwizard.metrics5.collectd;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Histogram;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricAttribute;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Snapshot;
import io.dropwizard.metrics5.Timer;
import org.collectd.api.ValueList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CollectdReporterTest {
    @RegisterExtension
    public static Receiver receiver = new Receiver(25826);

    private final MetricRegistry registry = new MetricRegistry();
    private CollectdReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = CollectdReporter.forRegistry(registry)
                .withHostName("eddie")
                .build(new Sender("localhost", 25826));
    }

    @Test
    void reportsByteGauges() throws Exception {
        reportsGauges((byte) 128);
    }

    @Test
    void reportsShortGauges() throws Exception {
        reportsGauges((short) 2048);
    }

    @Test
    void reportsIntegerGauges() throws Exception {
        reportsGauges(42);
    }

    @Test
    void reportsLongGauges() throws Exception {
        reportsGauges(Long.MAX_VALUE);
    }

    @Test
    void reportsFloatGauges() throws Exception {
        reportsGauges(0.25);
    }

    @Test
    void reportsDoubleGauges() throws Exception {
        reportsGauges(0.125d);
    }

    private <T extends Number> void reportsGauges(T value) throws Exception {
        reporter.report(
                map(MetricName.build("gauge"), () -> value),
                map(),
                map(),
                map(),
                map());

        assertThat(nextValues(receiver)).containsExactly(value.doubleValue());
    }

    @Test
    void reportsBooleanGauges() throws Exception {
        reporter.report(
                map(MetricName.build("gauge"), () -> true),
                map(),
                map(),
                map(),
                map());

        assertThat(nextValues(receiver)).containsExactly(1d);

        reporter.report(
                map(MetricName.build("gauge"), () -> false),
                map(),
                map(),
                map(),
                map());

        assertThat(nextValues(receiver)).containsExactly(0d);
    }

    @Test
    void doesNotReportStringGauges() throws Exception {
        reporter.report(
                map(MetricName.build("unsupported"), () -> "value"),
                map(),
                map(),
                map(),
                map());

        assertThat(receiver.next()).isNull();
    }

    @Test
    void reportsCounters() throws Exception {
        Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(42L);

        reporter.report(
                map(),
                map(MetricName.build("api", "rest", "requests", "count"), counter),
                map(),
                map(),
                map());

        assertThat(nextValues(receiver)).containsExactly(42d);
    }

    @Test
    void reportsMeters() throws Exception {
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
                map(MetricName.build("api", "rest", "requests"), meter),
                map());

        assertThat(nextValues(receiver)).containsExactly(1d);
        assertThat(nextValues(receiver)).containsExactly(2d);
        assertThat(nextValues(receiver)).containsExactly(3d);
        assertThat(nextValues(receiver)).containsExactly(4d);
        assertThat(nextValues(receiver)).containsExactly(5d);
    }

    @Test
    void reportsHistograms() throws Exception {
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
                map(MetricName.build("histogram"), histogram),
                map(),
                map());

        for (int i = 1; i <= 11; i++) {
            assertThat(nextValues(receiver)).containsExactly((double) i);
        }
    }

    @Test
    void reportsTimers() throws Exception {
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
                map(MetricName.build("timer"), timer));

        assertThat(nextValues(receiver)).containsExactly(1d);
        assertThat(nextValues(receiver)).containsExactly(100d);
        assertThat(nextValues(receiver)).containsExactly(200d);
        assertThat(nextValues(receiver)).containsExactly(300d);
        assertThat(nextValues(receiver)).containsExactly(400d);
        assertThat(nextValues(receiver)).containsExactly(500d);
        assertThat(nextValues(receiver)).containsExactly(600d);
        assertThat(nextValues(receiver)).containsExactly(700d);
        assertThat(nextValues(receiver)).containsExactly(800d);
        assertThat(nextValues(receiver)).containsExactly(900d);
        assertThat(nextValues(receiver)).containsExactly(1000d);
        assertThat(nextValues(receiver)).containsExactly(11d);
        assertThat(nextValues(receiver)).containsExactly(12d);
        assertThat(nextValues(receiver)).containsExactly(13d);
        assertThat(nextValues(receiver)).containsExactly(14d);
    }

    @Test
    void doesNotReportDisabledMetricAttributes() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getOneMinuteRate()).thenReturn(2.0);
        when(meter.getFiveMinuteRate()).thenReturn(3.0);
        when(meter.getFifteenMinuteRate()).thenReturn(4.0);
        when(meter.getMeanRate()).thenReturn(5.0);

        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(11L);

        CollectdReporter reporter = CollectdReporter.forRegistry(registry)
                .withHostName("eddie")
                .disabledMetricAttributes(EnumSet.of(MetricAttribute.M5_RATE, MetricAttribute.M15_RATE))
                .build(new Sender("localhost", 25826));

        reporter.report(
                map(),
                map(MetricName.build("counter"), counter),
                map(),
                map(MetricName.build("meter"), meter),
                map());

        assertThat(nextValues(receiver)).containsExactly(11d);
        assertThat(nextValues(receiver)).containsExactly(1d);
        assertThat(nextValues(receiver)).containsExactly(2d);
        assertThat(nextValues(receiver)).containsExactly(5d);
    }

    @Test
    void sanitizesMetricName() throws Exception {
        Counter counter = registry.counter("dash-illegal.slash/illegal");
        counter.inc();

        reporter.report();

        ValueList values = receiver.next();
        assertThat(values.getPlugin()).isEqualTo("dash_illegal.slash_illegal");
    }

    @Test
    void testUnableSetSecurityLevelToSignWithoutUsername() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                CollectdReporter.forRegistry(registry)
                        .withHostName("eddie")
                        .withSecurityLevel(SecurityLevel.SIGN)
                        .withPassword("t1_g3r")
                        .build(new Sender("localhost", 25826)))
                .withMessage("username is required for securityLevel: SIGN");
    }

    @Test
    void testUnableSetSecurityLevelToSignWithoutPassword() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                CollectdReporter.forRegistry(registry)
                        .withHostName("eddie")
                        .withSecurityLevel(SecurityLevel.SIGN)
                        .withUsername("scott")
                        .build(new Sender("localhost", 25826)))
                .withMessage("password is required for securityLevel: SIGN");
    }

    private <T> SortedMap<MetricName, T> map() {
        return Collections.emptySortedMap();
    }

    private <T> SortedMap<MetricName, T> map(MetricName name, T metric) {
        final Map<MetricName, T> map = Collections.singletonMap(name, metric);
        return new TreeMap<>(map);
    }

    @Test
    void sanitizesMetricNameWithCustomMaxLength() throws Exception {
        CollectdReporter customReporter = CollectdReporter.forRegistry(registry)
                .withHostName("eddie")
                .withMaxLength(20)
                .build(new Sender("localhost", 25826));

        Counter counter = registry.counter("dash-illegal.slash/illegal");
        counter.inc();

        customReporter.report();

        ValueList values = receiver.next();
        assertThat(values.getPlugin()).isEqualTo("dash_illegal.slash_i");
    }

    private List<Number> nextValues(Receiver receiver) throws Exception {
        final ValueList valueList = receiver.next();
        return valueList == null ? Collections.emptyList() : valueList.getValues();
    }
}
