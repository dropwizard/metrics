package com.codahale.metrics.chukwa;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

public class SocketReporterTest {
	private volatile boolean running = true;
	private Receiver receiver = new Receiver(9095);
	private Thread receiverThread = new Thread(receiver);
	private final MetricRegistry registry = mock(MetricRegistry.class);
	private final Clock clock = mock(Clock.class);
	private LoggingEvent event = null;
	private PatternLayout layout = new PatternLayout("%m%n");

	private final SocketReporter reporter = SocketReporter
			.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
			.convertDurationsTo(TimeUnit.MILLISECONDS).filter(MetricFilter.ALL)
			.withHost("localhost").withPort(9095).build();

	@Before
	public void setUp() throws Exception {
		when(clock.getTime()).thenReturn(1363568676000L);
		receiverThread.start();
	}

	@After
	public void tearDown() throws Exception {
		receiver.shutdown();
	}

	@Test
	public void reportsGaugeValues() throws Exception {
		final Gauge gauge = mock(Gauge.class);

		when(gauge.getValue()).thenReturn(1L);

		reporter.report(map("gauge", gauge), this.<Counter> map(),
				this.<Histogram> map(), this.<Meter> map(), this.<Timer> map());

		JSONObject json = (JSONObject) JSONValue.parse(getMetric());

		assertEquals(1L, json.get("value"));

		setEvent(null);
	}

	@Test
	public void reportsCounterValues() throws Exception {
		final Counter counter = mock(Counter.class);

		when(counter.getCount()).thenReturn(100L);

		reporter.report(this.<Gauge> map(), map("test.counter", counter),
				this.<Histogram> map(), this.<Meter> map(), this.<Timer> map());

		JSONObject json = (JSONObject) JSONValue.parse(getMetric());

		assertEquals(100L, json.get("value"));

		setEvent(null);
	}

	@Test
	public void reportsHistogramValues() throws Exception {
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

		reporter.report(this.<Gauge> map(), this.<Counter> map(),
				map("test.histogram", histogram), this.<Meter> map(),
				this.<Timer> map());

		JSONObject json = (JSONObject) JSONValue.parse(getMetric());

		assertEquals(3.0, json.get("mean"));
		assertEquals(9.0, json.get("p98"));
		assertEquals(11.0, json.get("p999"));
		assertEquals(5.0, json.get("stddev"));

		setEvent(null);
	}

	@Test
	public void reportsMeterValues() throws Exception {
		final Meter meter = mock(Meter.class);

		when(meter.getCount()).thenReturn(1L);
		when(meter.getMeanRate()).thenReturn(2.0);
		when(meter.getOneMinuteRate()).thenReturn(3.0);
		when(meter.getFiveMinuteRate()).thenReturn(4.0);
		when(meter.getFifteenMinuteRate()).thenReturn(5.0);

		reporter.report(this.<Gauge> map(), this.<Counter> map(),
				this.<Histogram> map(), map("test.meter", meter),
				this.<Timer> map());

		JSONObject json = (JSONObject) JSONValue.parse(getMetric());

		assertEquals(1L, json.get("count"));
		assertEquals(2.0, json.get("mean_rate"));
		assertEquals(3.0, json.get("m1"));
		assertEquals(5.0, json.get("m15"));

		setEvent(null);
	}

	@Test
	public void reportsTimerValues() throws Exception {
		final Timer timer = mock(Timer.class);

		when(timer.getCount()).thenReturn(1L);
		when(timer.getMeanRate()).thenReturn(2.0);
		when(timer.getOneMinuteRate()).thenReturn(3.0);
		when(timer.getFiveMinuteRate()).thenReturn(4.0);
		when(timer.getFifteenMinuteRate()).thenReturn(5.0);

		final Snapshot snapshot = mock(Snapshot.class);
		when(snapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
		when(snapshot.getMean()).thenReturn(
				(double) TimeUnit.MILLISECONDS.toNanos(200));
		when(snapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
		when(snapshot.getStdDev()).thenReturn(
				(double) TimeUnit.MILLISECONDS.toNanos(400));
		when(snapshot.getMedian()).thenReturn(
				(double) TimeUnit.MILLISECONDS.toNanos(500));
		when(snapshot.get75thPercentile()).thenReturn(
				(double) TimeUnit.MILLISECONDS.toNanos(600));
		when(snapshot.get95thPercentile()).thenReturn(
				(double) TimeUnit.MILLISECONDS.toNanos(700));
		when(snapshot.get98thPercentile()).thenReturn(
				(double) TimeUnit.MILLISECONDS.toNanos(800));
		when(snapshot.get99thPercentile()).thenReturn(
				(double) TimeUnit.MILLISECONDS.toNanos(900));
		when(snapshot.get999thPercentile()).thenReturn(
				(double) TimeUnit.MILLISECONDS.toNanos(1000));

		when(timer.getSnapshot()).thenReturn(snapshot);

		reporter.report(this.<Gauge> map(), this.<Counter> map(),
				this.<Histogram> map(), this.<Meter> map(),
				map("test.another.timer", timer));

		JSONObject json = (JSONObject) JSONValue.parse(getMetric());

		assertEquals(3.0, json.get("m1"));
		assertEquals(2.0, json.get("mean_rate"));
		assertEquals(5.0, json.get("m15"));
		assertEquals(900.0, json.get("p99"));

		setEvent(null);
	}

	private <T> SortedMap<String, T> map(String name, T metric) {
		final TreeMap<String, T> map = new TreeMap<String, T>();
		map.put(name, metric);
		return map;
	}

	private <T> SortedMap<String, T> map() {
		return new TreeMap<String, T>();
	}

	private class Receiver implements Runnable {
		private int port;
		private ServerSocket listener;

		public Receiver(int port) {
			this.port = port;
		}

		@Override
		public void run() {
			try {
				listener = new ServerSocket(port);
				Socket server = null;
				while (running) {
					server = listener.accept();
					Worker connection = new Worker(server);
					new Thread(connection).start();
				}
			} catch (IOException e) {

			}
		}

		public void shutdown() {
			try {
				if (listener != null) {
					listener.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}

	}

	private class Worker implements Runnable {
		private Socket connection = null;
		private ObjectInputStream inputStream = null;

		public Worker(Socket connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			try {
				inputStream = new ObjectInputStream(new BufferedInputStream(
						connection.getInputStream()));
				if (inputStream != null) {
					while (running) {
						LoggingEvent event = (LoggingEvent) inputStream
								.readObject();
						setEvent(event);
					}
				}
			} catch (IOException e) {
				System.out
						.println("Exception occurred while reading the object"
								+ e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (IOException e) {
					}
				}
			}
		}

	}

	public String getMetric() {
		try {
			while (event == null) {
				TimeUnit.SECONDS.sleep(2L);
			}
		} catch (InterruptedException e) {
			//ignore
		}

		byte[] bytes = layout.format(event).getBytes();
		String data = new String(bytes);
		return data;
	}

	public void setEvent(LoggingEvent event) {
		this.event = event;
	}
}
