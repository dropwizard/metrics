package com.yammer.metrics.core;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.management.*;

/**
 * A reporter which exposes application metric as JMX MBeans.
 *
 * @author coda
 */
public class JmxReporter implements Runnable {
	private static final ScheduledExecutorService TICK_THREAD =
			Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("metrics-jmx-reporter"));
	private final Map<MetricName, MetricMBean> beans;
	private final MBeanServer server;

	public static interface MetricMBean {
		public ObjectName objectName();
	}

	public static interface GaugeMBean extends MetricMBean {
		public Object getValue();
	}

	public static class Gauge implements GaugeMBean {
		private final ObjectName objectName;
		private final GaugeMetric<?> metric;

		public Gauge(GaugeMetric<?> metric, ObjectName objectName) {
			this.metric = metric;
			this.objectName = objectName;
		}

		@Override
		public ObjectName objectName() {
			return objectName;
		}

		@Override
		public Object getValue() {
			return metric.value();
		}
	}

	public static interface CounterMBean extends MetricMBean {
		public long getCount();
	}

	public static class Counter implements CounterMBean {
		private final ObjectName objectName;
		private final CounterMetric metric;

		public Counter(CounterMetric metric, ObjectName objectName) {
			this.metric = metric;
			this.objectName = objectName;
		}

		@Override
		public ObjectName objectName() {
			return objectName;
		}

		@Override
		public long getCount() {
			return metric.count();
		}
	}

	public static interface MeterMBean extends MetricMBean {
		public long getCount();
		public String getEventType();
		public TimeUnit getUnit();
		public double getMeanRate();
		public double getOneMinuteRate();
		public double getFiveMinuteRate();
		public double getFifteenMinuteRate();
	}

	public static class Meter implements MeterMBean {
		private final ObjectName objectName;
		private final MeterMetric metric;

		public Meter(MeterMetric metric, ObjectName objectName) {
			this.metric = metric;
			this.objectName = objectName;
		}

		@Override
		public ObjectName objectName() {
			return objectName;
		}

		@Override
		public long getCount() {
			return metric.count();
		}

		@Override
		public String getEventType() {
			return metric.eventType();
		}

		@Override
		public TimeUnit getUnit() {
			return metric.scaleUnit();
		}

		@Override
		public double getMeanRate() {
			return metric.meanRate();
		}

		@Override
		public double getOneMinuteRate() {
			return metric.oneMinuteRate();
		}

		@Override
		public double getFiveMinuteRate() {
			return metric.fiveMinuteRate();
		}

		@Override
		public double getFifteenMinuteRate() {
			return metric.fifteenMinuteRate();
		}
	}

	public static interface HistogramMBean extends MetricMBean {
		public long getCount();

		public double getMin();

		public double getMax();

		public double getMean();

		public double getStdDev();

		public double get50thPercentile();

		public double get75thPercentile();

		public double get95thPercentile();

		public double get98thPercentile();

		public double get99thPercentile();

		public double get999thPercentile();

		public List<Long> values();
	}

	public class Histogram implements HistogramMBean {
		private final ObjectName objectName;
		private final HistogramMetric metric;

		public Histogram(HistogramMetric metric, ObjectName objectName) {
			this.metric = metric;
			this.objectName = objectName;
		}

		@Override
		public ObjectName objectName() {
			return objectName;
		}

		@Override
		public double get50thPercentile() {
			return metric.percentiles(0.5)[0];
		}

		@Override
		public long getCount() {
			return metric.count();
		}

		@Override
		public double getMin() {
			return metric.min();
		}

		@Override
		public double getMax() {
			return metric.max();
		}

		@Override
		public double getMean() {
			return metric.mean();
		}

		@Override
		public double getStdDev() {
			return metric.stdDev();
		}

		@Override
		public double get75thPercentile() {
			return metric.percentiles(0.75)[0];
		}

		@Override
		public double get95thPercentile() {
			return metric.percentiles(0.95)[0];
		}

		@Override
		public double get98thPercentile() {
			return metric.percentiles(0.98)[0];
		}

		@Override
		public double get99thPercentile() {
			return metric.percentiles(0.99)[0];
		}

		@Override
		public double get999thPercentile() {
			return metric.percentiles(0.999)[0];
		}

		@Override
		public List<Long> values() {
			return metric.values();
		}
	}

	public static interface TimerMBean extends MetricMBean {
		public long getCount();

		public TimeUnit getRateUnit();

		public double getMeanRate();

		public double getOneMinuteRate();

		public double getFiveMinuteRate();

		public double getFifteenMinuteRate();

		public TimeUnit getLatencyUnit();

		public double getMin();

		public double getMax();

		public double getMean();

		public double getStdDev();

		public double get50thPercentile();

		public double get75thPercentile();

		public double get95thPercentile();

		public double get98thPercentile();

		public double get99thPercentile();

		public double get999thPercentile();

		public List<Double> values();
	}

	public class Timer implements TimerMBean {
		private final ObjectName objectName;
		private final TimerMetric metric;

		public Timer(TimerMetric metric, ObjectName objectName) {
			this.metric = metric;
			this.objectName = objectName;
		}

		@Override
		public ObjectName objectName() {
			return objectName;
		}

		@Override
		public double get50thPercentile() {
			return metric.percentiles(0.5)[0];
		}

		@Override
		public long getCount() {
			return metric.count();
		}

		@Override
		public TimeUnit getRateUnit() {
			return metric.rateUnit();
		}

		@Override
		public double getMeanRate() {
			return metric.meanRate();
		}

		@Override
		public double getOneMinuteRate() {
			return metric.oneMinuteRate();
		}

		@Override
		public double getFiveMinuteRate() {
			return metric.fiveMinuteRate();
		}

		@Override
		public double getFifteenMinuteRate() {
			return metric.fifteenMinuteRate();
		}

		@Override
		public TimeUnit getLatencyUnit() {
			return metric.durationUnit();
		}

		@Override
		public double getMin() {
			return metric.min();
		}

		@Override
		public double getMax() {
			return metric.max();
		}

		@Override
		public double getMean() {
			return metric.mean();
		}

		@Override
		public double getStdDev() {
			return metric.stdDev();
		}

		@Override
		public double get75thPercentile() {
			return metric.percentiles(0.75)[0];
		}

		@Override
		public double get95thPercentile() {
			return metric.percentiles(0.95)[0];
		}

		@Override
		public double get98thPercentile() {
			return metric.percentiles(0.98)[0];
		}

		@Override
		public double get99thPercentile() {
			return metric.percentiles(0.99)[0];
		}

		@Override
		public double get999thPercentile() {
			return metric.percentiles(0.999)[0];
		}

		@Override
		public List<Double> values() {
			return metric.values();
		}
	}

	/*package*/ JmxReporter() {
		this.beans = new HashMap<MetricName, MetricMBean>(Metrics.allMetrics().size());
		this.server = ManagementFactory.getPlatformMBeanServer();
	}

	public void start() {
		TICK_THREAD.scheduleAtFixedRate(this, 0, 1, TimeUnit.MINUTES);
		// then schedule the tick thread every 100ms for the next second so
		// as to pick up the initialization of most metrics (in the first 1s of
		// the application lifecycle) w/o incurring a high penalty later on
		for (int i = 1; i <= 900; i++) {
			TICK_THREAD.schedule(this, i * 100, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void run() {
		final Set<MetricName> newMetrics = new HashSet<MetricName>(Metrics.allMetrics().keySet());
		newMetrics.removeAll(beans.keySet());

		for (MetricName name : newMetrics) {
			final Metric metric = Metrics.allMetrics().get(name);
			if (metric != null) {
				try {
					final ObjectName objectName = new ObjectName(
							String.format("%s:type=%s,name=%s",
										  name.getKlass().getPackage().getName(),
										  name.getKlass().getSimpleName().replaceAll("\\$$", ""),
										  name.getName()));
					if (metric instanceof GaugeMetric) {
						registerBean(name, new Gauge((GaugeMetric<?>) metric, objectName), objectName);
					} else if (metric instanceof CounterMetric) {
						registerBean(name, new Counter((CounterMetric) metric, objectName), objectName);
					} else if (metric instanceof HistogramMetric) {
						registerBean(name, new Histogram((HistogramMetric) metric, objectName), objectName);
					} else if (metric instanceof MeterMetric) {
						registerBean(name, new Meter((MeterMetric) metric, objectName), objectName);
					} else if (metric instanceof TimerMetric) {
						registerBean(name, new Timer((TimerMetric) metric, objectName), objectName);
					}
				} catch (Exception ignored) {
				}
			}
		}
	}

	private void registerBean(MetricName name, MetricMBean bean, ObjectName objectName) throws MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
		beans.put(name, bean);
		server.registerMBean(bean, objectName);
	}

}
