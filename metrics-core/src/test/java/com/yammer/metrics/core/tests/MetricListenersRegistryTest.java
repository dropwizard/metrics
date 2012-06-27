package com.yammer.metrics.core.tests;

import static org.mockito.Mockito.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.CounterListener;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.HistogramListener;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MeterListener;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricListenersRegistry;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Stoppable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerListener;

public class MetricListenersRegistryTest {

	private interface StoppableCounterListener extends CounterListener,
			Stoppable {
	}

	private MetricListenersRegistry mlRegistry;

	private MetricsRegistry mRegistry;

	@After
	public void after() {
		mlRegistry.shutdown();
		mRegistry.shutdown();
	}

	@Before
	public void before() {

		mlRegistry = new MetricListenersRegistry();
		mRegistry = new MetricsRegistry();

		mRegistry.addListener(mlRegistry);
	}

	@Test
	public void cascadingShutdownListeners() {

		StoppableCounterListener stoppableListener = mock(StoppableCounterListener.class);
		CounterListener listener = mock(CounterListener.class);

		mlRegistry.addMetricListener(stoppableListener);
		mlRegistry.addMetricListener(listener);

		mlRegistry.shutdown();

		verify(stoppableListener).stop();
	}

	@Test
	public void receivingCounterEvents() {

		// add metric first
		Counter counter0 = mRegistry.newCounter(metricName());

		// add listener after the fact
		CounterListener listener = mock(CounterListener.class);
		when(listener.getMetricPredicate()).thenReturn(MetricPredicate.ALL);

		mlRegistry.addMetricListener(listener);

		counter0.inc(10);
		verify(listener).onUpdate(counter0, 10);

		// add metric after listener was already added
		Counter counter1 = mRegistry.newCounter(metricName());
		counter1.dec();
		verify(listener).onUpdate(counter1, -1);

		// clear all counters
		counter0.clear();
		counter1.clear();

		verify(listener).onClear(counter0);
		verify(listener).onClear(counter1);

		// remove listener
		mlRegistry.removeMetricListener(listener);
		counter0.clear();
		verify(listener, times(1)).onClear(counter0);
	}

	@Test
	public void receivingHistogramEvents() {

		// add metric first
		Histogram histogram0 = mRegistry.newHistogram(metricName(), false);

		// add listener after the fact
		HistogramListener listener = mock(HistogramListener.class);
		when(listener.getMetricPredicate()).thenReturn(MetricPredicate.ALL);

		mlRegistry.addMetricListener(listener);

		histogram0.update(10);
		verify(listener).onUpdate(histogram0, 10);

		// add metric after listener was already added
		Histogram histogram1 = mRegistry.newHistogram(metricName(), false);
		histogram1.update(-1);
		verify(listener).onUpdate(histogram1, -1);

		// clear all counters
		histogram0.clear();
		histogram1.clear();

		verify(listener).onClear(histogram0);
		verify(listener).onClear(histogram1);

		// remove listener
		mlRegistry.removeMetricListener(listener);
		histogram0.clear();
		verify(listener, times(1)).onClear(histogram0);
	}

	@Test
	public void receivingMeterEvents() {

		// add metric first
		Meter meter0 = mRegistry.newMeter(metricName(), "requests/sec",
				TimeUnit.SECONDS);

		// add listener after the fact
		MeterListener listener = mock(MeterListener.class);
		when(listener.getMetricPredicate()).thenReturn(MetricPredicate.ALL);

		mlRegistry.addMetricListener(listener);

		meter0.mark(10);
		verify(listener).onMark(meter0, 10);

		// add metric after listener was already added
		Meter meter1 = mRegistry.newMeter(metricName(), "requests/sec",
				TimeUnit.SECONDS);
		meter1.mark();
		verify(listener).onMark(meter1, 1);

		// remove listener
		mlRegistry.removeMetricListener(listener);
		meter0.mark();

		// only one call
		verify(listener, times(1)).onMark(meter0, 10);
	}

	@Test
	public void receivingTimerEvents() {

		// add metric first
		Timer timer0 = mRegistry.newTimer(metricName(), TimeUnit.MILLISECONDS,
				TimeUnit.SECONDS);

		// add listener after the fact
		TimerListener listener = mock(TimerListener.class);
		when(listener.getMetricPredicate()).thenReturn(MetricPredicate.ALL);

		mlRegistry.addMetricListener(listener);

		timer0.update(10, TimeUnit.MILLISECONDS);
		verify(listener).onUpdate(timer0, 10, TimeUnit.MILLISECONDS);

		// add metric after listener was already added
		Timer timer1 = mRegistry.newTimer(metricName(), TimeUnit.MILLISECONDS,
				TimeUnit.SECONDS);
		timer1.update(1, TimeUnit.SECONDS);
		verify(listener).onUpdate(timer1, 1, TimeUnit.SECONDS);

		// remove listener
		mlRegistry.removeMetricListener(listener);
		timer0.update(10, TimeUnit.MILLISECONDS);

		// only one call
		verify(listener, times(1)).onUpdate(timer0, 10, TimeUnit.MILLISECONDS);
	}

	@Test
	public void registeringForSpecificMetrics() {

		// add metrics first
		Counter counter0 = mRegistry.newCounter(metricName());
		Meter meter0 = mRegistry.newMeter(metricName(), "requests/sec",
				TimeUnit.SECONDS);

		// add counter listener after the fact
		CounterListener listener = mock(CounterListener.class);
		when(listener.getMetricPredicate()).thenReturn(new MetricPredicate() {
			@Override
			public boolean matches(final MetricName name, final Metric metric) {
				return metric.getClass() == Counter.class;
			}
		});

		mlRegistry.addMetricListener(listener);

		counter0.inc(10);
		meter0.mark();
		verify(listener).onUpdate(counter0, 10);

		// add metric after listener was already added
		Counter counter1 = mRegistry.newCounter(metricName());
		Meter meter1 = mRegistry.newMeter(metricName(), "requests/sec",
				TimeUnit.SECONDS);
		counter1.dec();
		meter1.mark();
		verify(listener).onUpdate(counter1, -1);
	}

	@Test(expected = IllegalStateException.class)
	public void registeringForUnsupportedMetrics() {

		// add metrics first
		mRegistry.newCounter(metricName());
		mRegistry.newMeter(metricName(), "requests/sec", TimeUnit.SECONDS);

		// add counter listener after the fact, with a loose MetricsPredicate
		CounterListener listener = mock(CounterListener.class);
		when(listener.getMetricPredicate()).thenReturn(MetricPredicate.ALL);

		// this will fail to add the listener to a non-counter metric
		mlRegistry.addMetricListener(listener);
	}

	@Test
	public void removingMetric() {

		// add metric first
		MetricName name = metricName();
		Counter counter0 = mRegistry.newCounter(name);

		// add listener after the fact
		CounterListener listener = mock(CounterListener.class);
		when(listener.getMetricPredicate()).thenReturn(MetricPredicate.ALL);

		mlRegistry.addMetricListener(listener);

		// remove metric, removes all listeners too
		mRegistry.removeMetric(name);
		counter0.clear();
		verify(listener, never()).onClear(counter0);
	}

	private MetricName metricName() {
		return new MetricName("group", "type", randomString());
	}

	private static String randomString() {

		final int length = 8;
		final char[] characters = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		final Random rnd = new Random();

		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters[rnd.nextInt(characters.length)];
		}

		return new String(text);
	}
}
