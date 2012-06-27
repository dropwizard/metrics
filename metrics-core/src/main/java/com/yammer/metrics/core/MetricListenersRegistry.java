package com.yammer.metrics.core;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A registry of metrics listeners. This is itself a listener on events of the
 * {@link MetricsRegistry} and an instance of this must be registered with the
 * {@link MetricsRegistry} as the bootstrapping mechanism.
 */
public class MetricListenersRegistry implements MetricsRegistryListener {

	private static final int EXPECTED_METRIC_COUNT = 1024;

	private final ConcurrentMap<MetricName, Metric> metrics;

	private final Set<CounterListener> counterListeners;

	private final Set<HistogramListener> histogramListeners;

	private final Set<MeterListener> meterListeners;

	private final Set<TimerListener> timerListeners;

	/**
	 * Default constructor. After instantiation, this should be registered with
	 * {@link MetricsRegistry#addListener(MetricsRegistryListener)}.
	 */
	public MetricListenersRegistry() {
		metrics = new ConcurrentHashMap<MetricName, Metric>(
				EXPECTED_METRIC_COUNT);

		counterListeners = new CopyOnWriteArraySet<CounterListener>();
		histogramListeners = new CopyOnWriteArraySet<HistogramListener>();
		meterListeners = new CopyOnWriteArraySet<MeterListener>();
		timerListeners = new CopyOnWriteArraySet<TimerListener>();
	}

	public void addMetricListener(final CounterListener counterListener) {
		addMetricListener(Counter.class, counterListener, counterListeners);
	}

	public void addMetricListener(final HistogramListener histogramListener) {
		addMetricListener(Histogram.class, histogramListener,
				histogramListeners);
	}

	public void addMetricListener(final MeterListener meterListener) {
		addMetricListener(Meter.class, meterListener, meterListeners);
	}

	public void addMetricListener(final TimerListener timerListener) {
		addMetricListener(Timer.class, timerListener, timerListeners);
	}

	@Override
	public void onMetricAdded(final MetricName name, final Metric metric) {

		metrics.put(name, metric);

		if (metric instanceof Counter) {
			onMetricAdded(name, metric, counterListeners);
		} else if (metric instanceof Histogram) {
			onMetricAdded(name, metric, histogramListeners);
		} else if (metric instanceof Meter) {
			onMetricAdded(name, metric, meterListeners);
		} else if (metric instanceof Timer) {
			onMetricAdded(name, metric, timerListeners);
		}
	}

	@Override
	public void onMetricRemoved(final MetricName name) {

		Metric metric = metrics.get(name);
		if (metric instanceof ObservableMetric<?>) {
			((ObservableMetric<?>) metric).removeAllListeners();
		}
	}

	public void removeMetricListener(final CounterListener counterListener) {
		removeMetricListener(counterListener, counterListeners);
	}

	public void removeMetricListener(final HistogramListener histogramListener) {
		removeMetricListener(histogramListener, histogramListeners);
	}

	public void removeMetricListener(final MeterListener meterListener) {
		removeMetricListener(meterListener, meterListeners);
	}

	public void removeMetricListener(final TimerListener timerListener) {
		removeMetricListener(timerListener, timerListeners);
	}

	/**
	 * Shut down all stoppable listeners. Allows them to cleanup thread pools,
	 * open connections, etc.
	 */
	public void shutdown() {
		shutdownListeners(counterListeners);
		shutdownListeners(histogramListeners);
		shutdownListeners(meterListeners);
		shutdownListeners(timerListeners);
	}

	@SuppressWarnings("unchecked")
	private <M extends Metric, L extends MetricListener> void addMetricListener(
			final Class<M> metricType, final L metricListener,
			final Set<L> metricListeners) {

		if (!metricListeners.add(metricListener)) {
			// already registered
			return;
		}

		// add listener to existing metrics that match the MetricPredicate
		for (Entry<MetricName, Metric> entry : metrics.entrySet()) {

			MetricName name = entry.getKey();
			Metric metric = entry.getValue();

			if (metricListener.getMetricPredicate().matches(name, metric)) {

				// sanity check of the listener's MetricPredicate
				if (metric instanceof ObservableMetric<?>
						&& metricType.isAssignableFrom(metric.getClass())) {
					((ObservableMetric<L>) metric).addListener(metricListener);
				} else {
					// something is wrong in the listener's MetricsPredicate
					throw new IllegalStateException(
							"Cannot register a listener since the MetricPredicate is not strict enough against checking for the right metric type. "
									+ "For example, a CounterListener's MetricPredicate should not match a Meter. "
									+ "Please double check the MetricPredicate returned from the listener class: "
									+ metricListener.getClass());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <L extends MetricListener> void onMetricAdded(
			final MetricName name, final Metric metric,
			final Set<L> metricListeners) {

		if (!(metric instanceof ObservableMetric<?>)) {
			return;
		}

		// add existing listeners to new metric
		for (L l : metricListeners) {
			if (l.getMetricPredicate().matches(name, metric)) {
				((ObservableMetric<L>) metric).addListener(l);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <L extends MetricListener> void removeMetricListener(
			final L metricListener, final Set<L> metricListeners) {

		if (!metricListeners.remove(metricListener)) {
			// never existed anyways
			return;
		}

		// remove listener from existing metrics that match the MetricPredicate
		for (Entry<MetricName, Metric> entry : metrics.entrySet()) {

			MetricName name = entry.getKey();
			Metric metric = entry.getValue();

			if (metricListener.getMetricPredicate().matches(name, metric)) {
				if (metric instanceof ObservableMetric<?>) {
					((ObservableMetric<L>) metric)
							.removeListener(metricListener);
				}
			}
		}
	}

	private <L extends MetricListener> void shutdownListeners(
			final Set<L> listeners) {

		for (L listener : listeners) {
			if (listener instanceof Stoppable) {
				((Stoppable) listener).stop();
			}
		}
	}
}
