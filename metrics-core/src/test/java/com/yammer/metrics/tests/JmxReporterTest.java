package com.yammer.metrics.tests;

import com.yammer.metrics.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.entry;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class JmxReporterTest {
    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private final String name = UUID.randomUUID().toString().replaceAll("[{\\-}]", "");
    private final MetricRegistry registry = spy(new MetricRegistry(name));

    private final JmxReporter reporter = JmxReporter.forRegistry(registry)
                                                    .registerWith(mBeanServer)
                                                    .filter(MetricFilter.ALL)
                                                    .build();

    private final Gauge gauge = mock(Gauge.class);
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);

    @Before
    public void setUp() throws Exception {
        when(gauge.getValue()).thenReturn(1);

        when(counter.getCount()).thenReturn(100L);

        when(histogram.getCount()).thenReturn(1L);
        when(histogram.getMax()).thenReturn(2L);
        when(histogram.getMean()).thenReturn(3.0);
        when(histogram.getMin()).thenReturn(4L);
        when(histogram.getStdDev()).thenReturn(5.0);

        final Snapshot hSnapshot = mock(Snapshot.class);
        when(hSnapshot.getMedian()).thenReturn(6.0);
        when(hSnapshot.get75thPercentile()).thenReturn(7.0);
        when(hSnapshot.get95thPercentile()).thenReturn(8.0);
        when(hSnapshot.get98thPercentile()).thenReturn(9.0);
        when(hSnapshot.get99thPercentile()).thenReturn(10.0);
        when(hSnapshot.get999thPercentile()).thenReturn(11.0);

        when(histogram.getSnapshot()).thenReturn(hSnapshot);

        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);

        when(timer.getCount()).thenReturn(1L);
        when(timer.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
        when(timer.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
        when(timer.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
        when(timer.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));

        when(timer.getMeanRate()).thenReturn(2.0);
        when(timer.getOneMinuteRate()).thenReturn(3.0);
        when(timer.getFiveMinuteRate()).thenReturn(4.0);
        when(timer.getFifteenMinuteRate()).thenReturn(5.0);

        final Snapshot tSnapshot = mock(Snapshot.class);
        when(tSnapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(tSnapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(tSnapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(tSnapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(tSnapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(tSnapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(1000));

        when(timer.getSnapshot()).thenReturn(tSnapshot);

        registry.register("gauge", gauge);
        registry.register("test.counter", counter);
        registry.register("test.histogram", histogram);
        registry.register("test.meter", meter);
        registry.register("test.another.timer", timer);

        reporter.start();
    }

    @After
    public void tearDown() throws Exception {
        reporter.stop();
    }

    @Test
    public void registersMBeansForGauges() throws Exception {
        final AttributeList attributes = getAttributes("gauge", "Value");

        assertThat(values(attributes))
                .contains(entry("Value", 1));
    }

    @Test
    public void registersMBeansForCounters() throws Exception {
        final AttributeList attributes = getAttributes("test.counter", "Count");

        assertThat(values(attributes))
                .contains(entry("Count", 100L));
    }

    @Test
    public void registersMBeansForHistograms() throws Exception {
        final AttributeList attributes = getAttributes("test.histogram",
                                                       "Count",
                                                       "Max",
                                                       "Mean",
                                                       "Min",
                                                       "StdDev",
                                                       "50thPercentile",
                                                       "75thPercentile",
                                                       "95thPercentile",
                                                       "98thPercentile",
                                                       "99thPercentile",
                                                       "999thPercentile");

        assertThat(values(attributes))
                .contains(entry("Count", 1L))
                .contains(entry("Max", 2L))
                .contains(entry("Mean", 3.0))
                .contains(entry("Min", 4L))
                .contains(entry("StdDev", 5.0))
                .contains(entry("50thPercentile", 6.0))
                .contains(entry("75thPercentile", 7.0))
                .contains(entry("95thPercentile", 8.0))
                .contains(entry("98thPercentile", 9.0))
                .contains(entry("99thPercentile", 10.0))
                .contains(entry("999thPercentile", 11.0));
    }

    @Test
    public void registersMBeansForMeters() throws Exception {
        final AttributeList attributes = getAttributes("test.meter",
                                                       "Count",
                                                       "MeanRate",
                                                       "OneMinuteRate",
                                                       "FiveMinuteRate",
                                                       "FifteenMinuteRate");

        assertThat(values(attributes))
                .contains(entry("Count", 1L))
                .contains(entry("MeanRate", 2.0))
                .contains(entry("OneMinuteRate", 3.0))
                .contains(entry("FiveMinuteRate", 4.0))
                .contains(entry("FifteenMinuteRate", 5.0));
    }

    @Test
    public void registersMBeansForTimers() throws Exception {
        final AttributeList attributes = getAttributes("test.another.timer",
                                                       "Count",
                                                       "MeanRate",
                                                       "OneMinuteRate",
                                                       "FiveMinuteRate",
                                                       "FifteenMinuteRate",
                                                       "Max",
                                                       "Mean",
                                                       "Min",
                                                       "StdDev",
                                                       "50thPercentile",
                                                       "75thPercentile",
                                                       "95thPercentile",
                                                       "98thPercentile",
                                                       "99thPercentile",
                                                       "999thPercentile");

        assertThat(values(attributes))
                .contains(entry("Count", 1L))
                .contains(entry("MeanRate", 2.0))
                .contains(entry("OneMinuteRate", 3.0))
                .contains(entry("FiveMinuteRate", 4.0))
                .contains(entry("FifteenMinuteRate", 5.0))
                .contains(entry("Max", 100000000L))
                .contains(entry("Mean", 2.0e8))
                .contains(entry("Min", 300000000L))
                .contains(entry("StdDev", 4.0e8))
                .contains(entry("50thPercentile", 5.0e8))
                .contains(entry("75thPercentile", 6.0e8))
                .contains(entry("95thPercentile", 7.0e8))
                .contains(entry("98thPercentile", 8.0e8))
                .contains(entry("99thPercentile", 9.0e8))
                .contains(entry("999thPercentile", 10.0e8));
    }

    @Test
    public void cleansUpAfterItselfWhenStopped() throws Exception {
        reporter.stop();

        try {
            getAttributes("gauge", "Value");
            failBecauseExceptionWasNotThrown(InstanceNotFoundException.class);
        } catch (InstanceNotFoundException e) {

        }
    }

    private AttributeList getAttributes(String name, String... attributeNames) throws JMException {
        final ObjectName n = new ObjectName(this.name, "name", name);
        return mBeanServer.getAttributes(n, attributeNames);
    }

    private SortedMap<String, Object> values(AttributeList attributes) {
        final TreeMap<String, Object> values = new TreeMap<String, Object>();
        for (Object o : attributes) {
            final Attribute attribute = (Attribute) o;
            values.put(attribute.getName(), attribute.getValue());
        }
        return values;
    }
}
