package com.yammer.metrics.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.util.MetricPredicate;

public class CsvReporter extends AbstractPollingReporter {
	private final MetricPredicate _predicate;
	private final File _outputDir;
	private final Map<MetricName, PrintStream> _streamMap;
	private long _startTime;

	public CsvReporter(File outputDir, MetricsRegistry metricsRegistry,
			MetricPredicate predicate) throws Exception {
		super(metricsRegistry, "csv-reporter");
		_outputDir = outputDir;
		_predicate = predicate;
		_streamMap = new HashMap<MetricName, PrintStream>();
		_startTime = 0L;
	}

	public CsvReporter(File outputDir, MetricsRegistry metricsRegistry)
			throws Exception {
		this(outputDir, metricsRegistry, MetricPredicate.ALL);
	}

	private PrintStream getPrintStream(MetricName metricName, Metric metric)
			throws IOException {
		PrintStream stream = null;
		synchronized (_streamMap) {
			stream = _streamMap.get(metricName);
			if (stream == null) {
				File newFile = new File(_outputDir, metricName.getName()
						+ ".csv");
				newFile.createNewFile();
				stream = new PrintStream(new FileOutputStream(newFile));
				_streamMap.put(metricName, stream);
				if (metric instanceof GaugeMetric<?>) {
					stream.println("# time,value");
				} else if (metric instanceof CounterMetric) {
					stream.println("# time,count");
				} else if (metric instanceof HistogramMetric) {
					stream.println("# time,min,max,mean,median,stddev,90%,95%,99%");
				} else if (metric instanceof MeterMetric) {
					stream.println("# time,count,1 min rate,mean rate,5 min rate,15 min rate");
				} else if (metric instanceof TimerMetric) {
					stream.println("# time,min,max,mean,median,stddev,90%,95%,99%");
				}
			}
		}
		return stream;
	}

	@Override
	public void run() {
		long time = (System.currentTimeMillis() - _startTime) / 1000;
		Set<Entry<MetricName, Metric>> metrics = metricsRegistry.allMetrics()
				.entrySet();
		try {
			for (Entry<MetricName, Metric> entry : metrics) {
				MetricName metricName = entry.getKey();
				Metric metric = entry.getValue();
				if (_predicate.matches(metricName, metric)) {
					StringBuilder buf = new StringBuilder();
					buf.append(time).append(",");
					if (metric instanceof GaugeMetric<?>) {
						Object objVal = ((GaugeMetric<?>) metric).value();
						buf.append(objVal);
					} else if (metric instanceof CounterMetric) {
						buf.append(((CounterMetric) metric).count());
					} else if (metric instanceof HistogramMetric) {
						HistogramMetric timer = (HistogramMetric) metric;

						final double[] percentiles = timer.percentiles(0.5,
								0.90, 0.95, 0.99);
						buf.append(timer.min()).append(",");
						buf.append(timer.max()).append(",");
						buf.append(timer.mean()).append(",");
						buf.append(percentiles[0]).append(","); // median
						buf.append(timer.stdDev()).append(",");
						buf.append(percentiles[1]).append(","); // 90%
						buf.append(percentiles[2]).append(","); // 95%
						buf.append(percentiles[3]); // 99 %
					} else if (metric instanceof MeterMetric) {
						buf.append(((MeterMetric) metric).count()).append(",");
						buf.append(((MeterMetric) metric).oneMinuteRate())
								.append(",");
						buf.append(((MeterMetric) metric).meanRate()).append(
								",");
						buf.append(((MeterMetric) metric).fiveMinuteRate())
								.append(",");
						buf.append(((MeterMetric) metric).fifteenMinuteRate());
					} else if (metric instanceof TimerMetric) {
						TimerMetric timer = (TimerMetric) metric;

						final double[] percentiles = timer.percentiles(0.5,
								0.90, 0.95, 0.99);
						buf.append(timer.min()).append(",");
						buf.append(timer.max()).append(",");
						buf.append(timer.mean()).append(",");
						buf.append(percentiles[0]).append(","); // median
						buf.append(timer.stdDev()).append(",");
						buf.append(percentiles[1]).append(","); // 90%
						buf.append(percentiles[2]).append(","); // 95%
						buf.append(percentiles[3]); // 99 %
					}

					PrintStream out = getPrintStream(metricName, metric);
					out.println(buf.toString());
					out.flush();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(long period, TimeUnit unit) {
		_startTime = System.currentTimeMillis();
		super.start(period, unit);
	}

	@Override
	public void shutdown() {
		try {
			super.shutdown();
		} finally {
			for (PrintStream out : _streamMap.values()) {
				out.close();
			}
		}
	}
}
