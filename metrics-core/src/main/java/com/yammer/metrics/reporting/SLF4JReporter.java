package com.yammer.metrics.reporting;

import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.Marker;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.stats.Snapshot;

public class SLF4JReporter extends AbstractPollingReporter implements
		MetricProcessor<Logger> {
	
	private Logger reportingLogger;
	private Marker reportingMarker;
	private MetricPredicate predicate;
	
	public SLF4JReporter(MetricsRegistry registry, String name, Logger reportingLogger, Marker reportingMarker, MetricPredicate predicate) {
		super(registry, name);
		this.reportingLogger = reportingLogger;
		this.reportingMarker = reportingMarker;
		this.predicate = predicate;		
	}
	
	public SLF4JReporter(String name, Logger reportingLogger){
		this(Metrics.defaultRegistry(), name, reportingLogger, null, MetricPredicate.ALL);
	}
	
	public SLF4JReporter(String name, Logger reportingLogger, Marker reportingMarker){
		this(Metrics.defaultRegistry(), name, reportingLogger, reportingMarker, MetricPredicate.ALL);
	}

	@Override
	public void run() {
		Set<Entry<MetricName, Metric>> metrics = getMetricsRegistry().getAllMetrics().entrySet();
		MetricDispatcher dispatcher = new MetricDispatcher();
		try {
            for (Entry<MetricName, Metric> entry : metrics) {
                final MetricName metricName = entry.getKey();
                final Metric metric = entry.getValue();
                if (predicate.matches(metricName, metric)) {                    
                    dispatcher.dispatch(entry.getValue(), entry.getKey(), this, reportingLogger);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}



	@Override
	public void processMeter(MetricName name, Metered meter, Logger context)
			throws Exception {
		Object[] values = { name.getDomain() , name.getScope() , name.getName() , meter.getCount() , meter.getOneMinuteRate() , meter.getMeanRate() , meter.getFiveMinuteRate() , meter.getFifteenMinuteRate() };
		context.info(reportingMarker, "type=METER , domain={} , scope={} , name={} , count={} , 1_min_rate={} , mean_rate={} , 5_min_rate={} , 15_min_rate={}", values);		
	}



	@Override
	public void processCounter(MetricName name, Counter counter, Logger context)
			throws Exception {
		Object[] values = { name.getDomain() , name.getScope() , name.getName() , counter.getCount() };
		context.info(reportingMarker, "type=COUNTER , domain={} , scope={} , name={} , count={} ", values);
	}



	@Override
	public void processHistogram(MetricName name, Histogram histogram, Logger context) throws Exception {		
		final Snapshot snapshot = histogram.getSnapshot();
		Object[] values = { name.getDomain() , name.getScope() , name.getName(), histogram.getMin(), histogram.getMax(), histogram.getMean(), histogram.getStdDev(), snapshot.getMedian(), snapshot.get75thPercentile(), snapshot.get95thPercentile(), snapshot.get99thPercentile(), snapshot.get999thPercentile() };
		context.info(reportingMarker, "type=HISTOGRAM , domain={} , scope={} , name={} , min={} , max={} , mean={} , stddev={} , median={} , 75_pct={}, 95_pct={} , 99_pct={} , 99_9_pct={}", values);
	}



	@Override
	public void processTimer(MetricName name, Timer timer, Logger context)
			throws Exception {
		final Snapshot snapshot = timer.getSnapshot();
		Object[] values = { name.getDomain() , name.getScope() , name.getName(), timer.getDurationUnit(), timer.getMin(), timer.getMax(), timer.getMean(), timer.getStdDev(), snapshot.getMedian(), snapshot.get75thPercentile(), snapshot.get95thPercentile(), snapshot.get99thPercentile(), snapshot.get999thPercentile() };
		context.info(reportingMarker, "type=TIMER , domain={} , scope={} , name={} , time_unit={} , min={} , max={} , mean={} , stddev={} , median={} , 75_pct={}, 95_pct={} , 99_pct={} , 99_9_pct={}", values);		
	}



	@Override
	public void processGauge(MetricName name, Gauge<?> gauge, Logger context)
			throws Exception {
		Object[] values = { name.getDomain() , name.getScope() , name.getName() , gauge.getValue() };
		context.info(reportingMarker, "type=GAUGE , domain={} , scope={} ,  name={} , value={} ", values);
	}

}
