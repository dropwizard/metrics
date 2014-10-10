package com.codahale.metrics;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SocketReporterTest {
	private volatile boolean running = true;
	private Receiver receiver = new Receiver(9095);
	private Thread receiverThread = new Thread(receiver);
	private final MetricRegistry registry = mock(MetricRegistry.class);
	private final Clock clock = mock(Clock.class);
	private String message = null;
	
	private final SocketReporter reporter = SocketReporter.forRegistry(registry)
															.formattedFor(TimeZone.getTimeZone("PST"))
															.convertRatesTo(TimeUnit.SECONDS)
															.convertDurationsTo(TimeUnit.MILLISECONDS).filter(MetricFilter.ALL)
															.withHost("localhost").withPort("9095").build();

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
		final String testString = "[type=GAUGE, name=gauge, value=1]";
		when(gauge.getValue()).thenReturn(1);

		reporter.report(map("gauge", gauge), this.<Counter> map(),
				this.<Histogram> map(), this.<Meter> map(), this.<Timer> map());

		assertEquals(testString, getMessage());
	}

	@Test
	public void reportsCounterValues() throws Exception {
		final Counter counter = mock(Counter.class);
		final String testString = "[type=COUNTER, name=test.counter, count=100]";
		when(counter.getCount()).thenReturn(100L);

		reporter.report(this.<Gauge> map(), map("test.counter", counter),
				this.<Histogram> map(), this.<Meter> map(), this.<Timer> map());
		assertEquals(testString, getMessage());

	}
	
	@Test
    public void reportsHistogramValues() throws Exception {
        final Histogram histogram = mock(Histogram.class);
		final String testString = "[type=HISTOGRAM, name=test.histogram, count=1, min=4, max=2, mean=3.0, stddev=5.0, median=6.0, p75=7.0, p95=8.0, p98=9.0, p99=10.0, p999=11.0]";
        
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

        reporter.report(this.<Gauge>map(),
                        this.<Counter>map(),
                        map("test.histogram", histogram),
                        this.<Meter>map(),
                        this.<Timer>map());
        assertEquals(testString, getMessage());

    }

    @Test
    public void reportsMeterValues() throws Exception {
        final Meter meter = mock(Meter.class);
        final String testString = "[type=METER, name=test.meter, count=1, mean_rate=2.0, m1=3.0, m5=4.0, m15=5.0, rate_unit=second]";
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

        assertEquals(testString, getMessage());
    }

    @Test
    public void reportsTimerValues() throws Exception {
        final Timer timer = mock(Timer.class);
		final String testString = "[type=TIMER, name=test.another.timer, count=1.0E-6, min=300.0, max=100.0, mean=200.0, stddev=400.0, median=500.0, p75=600.0, p95=700.0, p98=800.0, p99=900.0, p999=1000.0, mean_rate=2.0, m1=3.0, m5=4.0, m15=5.0, rate_unit=second, duration_unit=milliseconds]";
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
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS
                                                                        .toNanos(1000));

        when(timer.getSnapshot()).thenReturn(snapshot);

        reporter.report(this.<Gauge>map(),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        map("test.another.timer", timer));

        assertEquals(testString, getMessage());
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
						String msg = (String) inputStream.readObject();
						setMessage(msg);
						break;
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
	public String getMessage() {
		try {
			while(message == null) {
				Thread.currentThread().sleep(2000L);
			}
		}catch(InterruptedException e) {}
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
