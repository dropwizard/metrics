package io.dropwizard.metrics.collectd;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import io.dropwizard.metrics.Clock;
import io.dropwizard.metrics.Counter;
import io.dropwizard.metrics.Gauge;
import io.dropwizard.metrics.Histogram;
import io.dropwizard.metrics.Meter;
import io.dropwizard.metrics.MetricFilter;
import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.Snapshot;
import io.dropwizard.metrics.Timer;

public class CollectdReporterTest {
	private final long timestamp = 1000198;
	private final Clock clock = mock(Clock.class);
	private final Collectd collectd = mock(Collectd.class);
	private final MetricRegistry registry = mock(MetricRegistry.class);
	private final CollectdReporter reporter = CollectdReporter.forRegistry(registry).withClock(clock)
			.withPluginInstance("pluginInstance").convertRatesTo(TimeUnit.SECONDS)
			.convertDurationsTo(TimeUnit.MILLISECONDS).filter(MetricFilter.ALL).build(collectd);

	@Before
	public void setUp() throws Exception {
		when(clock.getTime()).thenReturn(timestamp * 1000);
	}

	@Test
	public void doesNotReportStringGaugeValues() throws Exception {
		reporter.report(map("gauge", gauge("value")), this.<Counter> map(), this.<Histogram> map(), this.<Meter> map(),
				this.<Timer> map());

		final InOrder inOrder = inOrder(collectd);
		inOrder.verify(collectd).isConnected();
		inOrder.verify(collectd).connect();
		inOrder.verify(collectd, never()).send(any(Packet.class));
		inOrder.verify(collectd).flush();

		verifyNoMoreInteractions(collectd);
	}

	@Test
	public void reportsNumericGaugeValues() throws Exception {
		reporter.report(map("gauge", gauge((byte) 1)), this.<Counter> map(), this.<Histogram> map(), this.<Meter> map(),
				this.<Timer> map());

		final InOrder inOrder = inOrder(collectd);
		inOrder.verify(collectd).isConnected();
		inOrder.verify(collectd).connect();
		inOrder.verify(collectd).send(any(Packet.class));
		inOrder.verify(collectd).flush();

		verifyNoMoreInteractions(collectd);
	}

	@Test
	public void reportsCounters() throws Exception {
		final Counter counter = mock(Counter.class);
		when(counter.getCount()).thenReturn(100L);

		reporter.report(this.<Gauge> map(), this.<Counter> map("counter", counter), this.<Histogram> map(),
				this.<Meter> map(), this.<Timer> map());

		final InOrder inOrder = inOrder(collectd);
		inOrder.verify(collectd).isConnected();
		inOrder.verify(collectd).connect();
		inOrder.verify(collectd).send(any(Packet.class));
		inOrder.verify(collectd).flush();

		verifyNoMoreInteractions(collectd);
	}

	@Test
	public void reportsHistograms() throws Exception {
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

		when(histogram.getSnapshot()).thenReturn(snapshot);

		reporter.report(this.<Gauge> map(), this.<Counter> map(), this.<Histogram> map("histogram", histogram),
				this.<Meter> map(), this.<Timer> map());

		final InOrder inOrder = inOrder(collectd);
		inOrder.verify(collectd).isConnected();
		inOrder.verify(collectd).connect();
		inOrder.verify(collectd).send(any(Packet.class));
		inOrder.verify(collectd).flush();

		verifyNoMoreInteractions(collectd);
	}

	@Test
	public void reportsMeters() throws Exception {
		final Meter meter = mock(Meter.class);
		when(meter.getCount()).thenReturn(1L);
		when(meter.getOneMinuteRate()).thenReturn(2.0);
		when(meter.getFiveMinuteRate()).thenReturn(3.0);
		when(meter.getFifteenMinuteRate()).thenReturn(4.0);
		when(meter.getMeanRate()).thenReturn(5.0);

		reporter.report(this.<Gauge> map(), this.<Counter> map(), this.<Histogram> map(),
				this.<Meter> map("meter", meter), this.<Timer> map());

		final InOrder inOrder = inOrder(collectd);
		inOrder.verify(collectd).isConnected();
		inOrder.verify(collectd).connect();
		inOrder.verify(collectd).send(any(Packet.class));
		inOrder.verify(collectd).flush();

		verifyNoMoreInteractions(collectd);
	}

	@Test
	public void reportsTimers() throws Exception {
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

		when(timer.getSnapshot()).thenReturn(snapshot);

		reporter.report(this.<Gauge> map(), this.<Counter> map(), this.<Histogram> map(), this.<Meter> map(),
				map("timer", timer));

		final InOrder inOrder = inOrder(collectd);
		inOrder.verify(collectd).isConnected();
		inOrder.verify(collectd).connect();
		inOrder.verify(collectd, times(2)).send(any(Packet.class));
		inOrder.verify(collectd).flush();

		verifyNoMoreInteractions(collectd);
	}

	@Test
	public void closesConnectionIfCollectdIsUnavailable() throws Exception {
		doThrow(new UnknownHostException("UNKNOWN-HOST")).when(collectd).connect();
		reporter.report(map("gauge", gauge(1)), this.<Counter> map(), this.<Histogram> map(), this.<Meter> map(),
				this.<Timer> map());

		final InOrder inOrder = inOrder(collectd);
		inOrder.verify(collectd).isConnected();
		inOrder.verify(collectd).connect();
		inOrder.verify(collectd).close();

		verifyNoMoreInteractions(collectd);
	}

	@Test
	public void closesConnectionIfAnUnexpectedExceptionOccurs() throws Exception {
		final Gauge gauge = mock(Gauge.class);
		when(gauge.getValue()).thenThrow(new RuntimeException("kaboom"));

		reporter.report(map("gauge", gauge), this.<Counter> map(), this.<Histogram> map(), this.<Meter> map(),
				this.<Timer> map());

		final InOrder inOrder = inOrder(collectd);
		inOrder.verify(collectd).isConnected();
		inOrder.verify(collectd).connect();
		inOrder.verify(collectd).close();

		verifyNoMoreInteractions(collectd);
	}

	@Test
	public void closesConnectionOnReporterStop() throws Exception {
		reporter.stop();

		verify(collectd).close();

		verifyNoMoreInteractions(collectd);
	}

	private <T> SortedMap<MetricName, T> map() {
		return new TreeMap<MetricName, T>();
	}

	private <T> SortedMap<MetricName, T> map(final String name, final T metric) {
		final TreeMap<MetricName, T> map = new TreeMap<MetricName, T>();
		map.put(MetricName.build(name), metric);
		return map;
	}

	private <T> Gauge gauge(final T value) {
		final Gauge gauge = mock(Gauge.class);
		when(gauge.getValue()).thenReturn(value);
		return gauge;
	}
}
