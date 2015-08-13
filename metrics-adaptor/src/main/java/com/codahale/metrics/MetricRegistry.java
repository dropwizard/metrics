package com.codahale.metrics;

import io.dropwizard.metrics.MetricName;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class MetricRegistry {
	private static final Logger LOG = LoggerFactory.getLogger(MetricRegistry.class);
	
	final io.dropwizard.metrics.MetricRegistry reg;

	public static String name(Class<?> klass, String... names) {
		return io.dropwizard.metrics.MetricRegistry
				.name(klass.getName(), names).getKey();
	}

	public static String name(String name, String... names) {
		return io.dropwizard.metrics.MetricRegistry.name(name, names).getKey();
	}

	public MetricRegistry(io.dropwizard.metrics.MetricRegistry reg) {
		this.reg = reg;
	}

	public static MetricRegistry of(io.dropwizard.metrics.MetricRegistry reg) {
		return new MetricRegistry(reg);
	}

	public <T extends Metric> T register(String name, T metric)
			throws IllegalArgumentException {
		if (metric instanceof MetricSet) {
			registerAll(MetricName.build(name), (MetricSet) metric);
		} else {
			reg.register(name, metric);
		}

		return metric;
	}

	public void registerAll(MetricSet metrics) throws IllegalArgumentException {
		registerAll(null, metrics);
	}

	private void registerAll(MetricName prefix, MetricSet metrics)
			throws IllegalArgumentException {
		if (prefix == null)
			prefix = MetricName.EMPTY;

		for (Map.Entry<String, Metric> entry : metrics.getMetrics().entrySet()) {
			if (entry.getValue() instanceof MetricSet) {
				registerAll(
						MetricName.join(prefix,
								MetricName.build(entry.getKey())),
						(MetricSet) entry.getValue());
			} else {
				reg.register(
						MetricName.join(prefix,
								MetricName.build(entry.getKey())),
						entry.getValue());
			}
		}
	}

	public Counter counter(String name) {
		return new Counter(reg.counter(MetricName.build(name)));
	}

	public Histogram histogram(String name) {
		return new Histogram(reg.histogram(MetricName.build(name)));
	}

	public Meter meter(String name) {
		return new Meter(reg.meter(MetricName.build(name)));
	}

	public Timer timer(String name) {
		return new Timer(reg.timer(MetricName.build(name)));
	}

	public boolean remove(String name) {
		return reg.remove(MetricName.build(name));
	}

	public void removeMatching(MetricFilter filter) {
		reg.removeMatching(transformFilter(filter));
	}

//	public void addListener(MetricRegistryListener listener) {
//		listeners.add(listener);
//
//		for (Map.Entry<MetricName, Metric> entry : metrics.entrySet()) {
//			notifyListenerOfAddedMetric(listener, entry.getValue(),
//					entry.getKey());
//		}
//	}
//
//	public void removeListener(MetricRegistryListener listener) {
//		listeners.remove(listener);
//	}

	public SortedSet<String> getNames() {
		SortedSet<String> names = new TreeSet<>();
		for(MetricName name: reg.getNames()){
			names.add(name.getKey());
		}
		return Collections.unmodifiableSortedSet(names);
	}

	@SuppressWarnings("rawtypes")
	public SortedMap<String, Gauge> getGauges() {
		return getGauges(MetricFilter.ALL);
	}

	@SuppressWarnings("rawtypes")
	public SortedMap<String, Gauge> getGauges(MetricFilter filter) {
		return adaptMetrics(Gauge.class, reg.getGauges(transformFilter(filter)));
	}

	public SortedMap<String, Counter> getCounters() {
		return getCounters(MetricFilter.ALL);
	}

	public SortedMap<String, Counter> getCounters(MetricFilter filter) {
		return adaptMetrics(Counter.class, reg.getCounters(transformFilter(filter)));
	}

	public SortedMap<String, Histogram> getHistograms() {
		return getHistograms(MetricFilter.ALL);
	}

	public SortedMap<String, Histogram> getHistograms(MetricFilter filter) {
		return adaptMetrics(Histogram.class, reg.getHistograms(transformFilter(filter)));
	}

	public SortedMap<String, Meter> getMeters() {
		return getMeters(MetricFilter.ALL);
	}

	public SortedMap<String, Meter> getMeters(MetricFilter filter) {
		return adaptMetrics(Meter.class, reg.getMeters(transformFilter(filter)));
	}

	public SortedMap<String, Timer> getTimers() {
		return getTimers(MetricFilter.ALL);
	}

	public SortedMap<String, Timer> getTimers(MetricFilter filter) {
		return adaptMetrics(Timer.class, reg.getTimers(transformFilter(filter)));
	}

	private io.dropwizard.metrics.MetricFilter transformFilter(final MetricFilter filter) {
		return new io.dropwizard.metrics.MetricFilter() {
			@Override
			public boolean matches(MetricName name, io.dropwizard.metrics.Metric metric) {
				try {
					return filter.matches(name.getKey(), adaptMetric(metric));
				} catch (ClassNotFoundException e) {
					LOG.warn("Came accross unadapted metric", e);
					return false;
				}
			}
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Metric adaptMetric(final io.dropwizard.metrics.Metric metric) throws ClassNotFoundException {
		if(metric instanceof Metric){
			return (Metric) metric;
		} else if(metric instanceof io.dropwizard.metrics.Counter){
			return new Counter((io.dropwizard.metrics.Counter) metric);
		} else if(metric instanceof io.dropwizard.metrics.Histogram){
			return new Histogram((io.dropwizard.metrics.Histogram) metric);
		} else if(metric instanceof io.dropwizard.metrics.Meter){
			return new Meter((io.dropwizard.metrics.Meter) metric);
		} else if(metric instanceof io.dropwizard.metrics.Timer){
			return new Timer((io.dropwizard.metrics.Timer) metric);
		} else if(metric instanceof io.dropwizard.metrics.Gauge){
			return new Gauge((io.dropwizard.metrics.Gauge) metric);
		}
		
		throw new ClassNotFoundException("Can't find adaptor class for metric of type "+ metric.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	private <T extends Metric, A extends io.dropwizard.metrics.Metric> SortedMap<String, T> adaptMetrics(final Class<T> klass, final SortedMap<MetricName, A> metrics) {
		SortedMap<String, T> items = new TreeMap<>();
		for(Map.Entry<MetricName, A> metric: metrics.entrySet()){
			try {
				items.put(metric.getKey().getKey(), (T) adaptMetric(metric.getValue()));
			} catch (ClassNotFoundException e) {
				LOG.warn("Came accross unadapted metric", e);
			}
		}
		
		return Collections.unmodifiableSortedMap(items);
	}
}
