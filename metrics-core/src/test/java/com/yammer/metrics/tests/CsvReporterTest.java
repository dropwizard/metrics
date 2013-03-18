package com.yammer.metrics.tests;

import com.yammer.metrics.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.CharBuffer;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CsvReporterTest {
    @Rule public final TemporaryFolder folder = new TemporaryFolder();

    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Clock clock = mock(Clock.class);

    private File dataDirectory;
    private CsvReporter reporter;

    @Before
    public void setUp() throws Exception {
        when(clock.getTime()).thenReturn(19910191000L);

        this.dataDirectory = folder.newFolder();

        this.reporter = new CsvReporter(registry,
                                        dataDirectory,
                                        Locale.US,
                                        TimeUnit.SECONDS,
                                        TimeUnit.MILLISECONDS,
                                        clock);
    }

    @Test
    public void reportsGaugeValues() throws Exception {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(1);

        reporter.report(map("gauge", gauge),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        assertThat(fileContents("gauge.csv"))
                .isEqualTo(csv(
                        "t,value",
                        "19910191,1"
                ));
    }

    @Test
    public void reportsCounterValues() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        reporter.report(this.<Gauge>map(),
                        map("test.counter", counter),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        assertThat(fileContents("test.counter.csv"))
                .isEqualTo(csv(
                        "t,count",
                        "19910191,100"
                ));
    }

    @Test
    public void reportsHistogramValues() throws Exception {
        final Histogram histogram = mock(Histogram.class);
        when(histogram.getCount()).thenReturn(1L);
        when(histogram.getMax()).thenReturn(2L);
        when(histogram.getMean()).thenReturn(3.0);
        when(histogram.getMin()).thenReturn(4L);
        when(histogram.getStdDev()).thenReturn(5.0);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMedian()).thenReturn(6.0);
        when(snapshot.get75thPercentile()).thenReturn(7.0);
        when(snapshot.get95thPercentile()).thenReturn(8.0);
        when(snapshot.get98thPercentile()).thenReturn(9.0);
        when(snapshot.get99thPercentile()).thenReturn(10.0);
        when(snapshot.get999thPercentile()).thenReturn(11.0);

        when(histogram.getSnapshot()).thenReturn(snapshot);

        reporter.report(this.<Gauge>map(),
                        this.<Counter>map(),
                        map("test.histogram", histogram),
                        this.<Meter>map(),
                        this.<Timer>map());

        assertThat(fileContents("test.histogram.csv"))
                .isEqualTo(csv(
                        "t,count,max,mean,min,stddev,p50,p75,p95,p98,p99,p999",
                        "19910191,1,2,3.000000,4,5.000000,6.000000,7.000000,8.000000,9.000000,10.000000,11.000000"
                ));
    }

    @Test
    public void reportsMeterValues() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);

        reporter.report(this.<Gauge>map(),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        map("test.meter", meter),
                        this.<Timer>map());

        assertThat(fileContents("test.meter.csv"))
                .isEqualTo(csv(
                        "t,count,mean_rate,m1_rate,m5_rate,m15_rate,rate_unit",
                        "19910191,1,2.000000,3.000000,4.000000,5.000000,events/second"
                ));
    }

    @Test
    public void reportsTimerValues() throws Exception {
        final Timer timer = mock(Timer.class);
        when(timer.getCount()).thenReturn(1L);
        when(timer.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
        when(timer.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
        when(timer.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
        when(timer.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));

        when(timer.getMeanRate()).thenReturn(2.0);
        when(timer.getOneMinuteRate()).thenReturn(3.0);
        when(timer.getFiveMinuteRate()).thenReturn(4.0);
        when(timer.getFifteenMinuteRate()).thenReturn(5.0);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(1000));

        when(timer.getSnapshot()).thenReturn(snapshot);

        reporter.report(this.<Gauge>map(),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        map("test.another.timer", timer));

        assertThat(fileContents("test.another.timer.csv"))
                .isEqualTo(csv(
                        "t,count,max,mean,min,stddev,p50,p75,p95,p98,p99,p999,mean_rate,m1_rate,m5_rate,m15_rate,rate_unit,duration_unit",
                        "19910191,1,100.000000,200.000000,300.000000,400.000000,500.000000,600.000000,700.000000,800.000000,900.000000,1000.000000,2.000000,3.000000,4.000000,5.000000,calls/second,milliseconds"
                ));
    }

    private String csv(String... lines) {
        final StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line).append(String.format("%n"));
        }
        return builder.toString();
    }

    private String fileContents(String filename) throws IOException {
        final StringBuilder builder = new StringBuilder();
        final FileInputStream input = new FileInputStream(new File(dataDirectory, filename));
        try {
            final InputStreamReader reader = new InputStreamReader(input);
            final BufferedReader bufferedReader = new BufferedReader(reader);
            final CharBuffer buf = CharBuffer.allocate(1024);
            while (bufferedReader.read(buf) != -1) {
                buf.flip();
                builder.append(buf);
                buf.clear();
            }
        } finally {
            input.close();
        }
        return builder.toString();
    }

    private <T> SortedMap<String, T> map() {
        return new TreeMap<String, T>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<String, T>();
        map.put(name, metric);
        return map;
    }
}
