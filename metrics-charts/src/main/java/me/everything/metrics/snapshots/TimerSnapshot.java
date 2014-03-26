package me.everything.metrics.snapshots;

import java.util.HashMap;
import java.util.Map;

import android.util.SparseArray;

import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

public class TimerSnapshot extends MetricSnapshot {

	public static final String MetricType = "Timer";
	
	public final long count;		
	private final double rateMean, rate1min, rate5min, rate15min;	
	private final double min, max, mean, stddev, p25, p50, p75, p90, p95, p99;
	private final String rateUnit;
	private final String durationUnit;
	
	public TimerSnapshot(String name, Timer timer, String rateUnit, String durationUnit) {
		this(name, timer, rateUnit, durationUnit, 1, 1);
	}
	
	public TimerSnapshot(String name, Timer timer, String rateUnit, String durationUnit, double rateFactor, double durationFactor) {
		super(name);
		
		this.rateUnit = rateUnit;
		this.durationUnit = durationUnit;
		
		count = timer.getCount();
		
		Snapshot snapshot = timer.getSnapshot();
		min = snapshot.getMin() * durationFactor;
		max = snapshot.getMax() * durationFactor;
		mean = snapshot.getMean() * durationFactor;
		stddev = snapshot.getStdDev() * durationFactor;
		p25 = snapshot.getValue(0.25) * durationFactor;
		p50 = snapshot.getMedian() * durationFactor;
		p75 = snapshot.get75thPercentile() * durationFactor;
		p90 = snapshot.getValue(0.9) * durationFactor;
		p95 = snapshot.get95thPercentile() * durationFactor;
		p99 = snapshot.get99thPercentile() * durationFactor;
		
		rateMean = rateFactor * timer.getMeanRate();
		rate1min = rateFactor * timer.getOneMinuteRate();
		rate5min = rateFactor * timer.getFiveMinuteRate();
		rate15min = rateFactor * timer.getFifteenMinuteRate();
	}

	@Override
	public String metricType() {
		return MetricType;
	}

	@Override
	public long count() {		
		return count;
	}

	public double min() {
		return min;
	}

	public double max() {
		return max;
	}

	public double mean() {
		return mean;
	}

	public double stddev() {
		return stddev;
	}

	public double p25() {
		return p25;
	}
	
	public double p50() {
		return p50;
	}

	public double p75() {
		return p75;
	}

	public double p90() {
		return p90;
	}

	public double p95() {
		return p95;
	}

	public double p99() {
		return p99;
	}
	
	public double rateMean() {
		return rateMean;
	}

	public double rate1min() {
		return rate1min;
	}

	public double rate5min() {
		return rate5min;
	}

	public double rate15min() {
		return rate15min;
	}

	public String rateUnit() {
		return rateUnit;
	}

	public String durationUnit() {
		return durationUnit;
	}

	@Override
	public Map<String, Double> allValues() {
		Map<String, Double> values = new HashMap<String, Double>();
		values.put("count", (double)count());
		values.put("min", min());
		values.put("max", max());
		values.put("mean", mean());
		values.put("p25", p25());
		values.put("p50", p50());
		values.put("p75", p75());
		values.put("p90", p90());
		values.put("rateMean", rateMean());
		values.put("rate1min", rate1min());
		values.put("rate5min", rate5min());
		values.put("rate15min", rate15min());
		return values;
	}

	@Override
	public SparseArray<Double> percentileValues() {
		SparseArray<Double> percentiles = new SparseArray<Double>();
		percentiles.put(0, min());
		percentiles.put(25, p25());
		percentiles.put(50, p50());
		percentiles.put(75, p75());
		percentiles.put(90, p90());
		percentiles.put(95, p95());
		percentiles.put(99, p99());
		percentiles.put(100, max());
		return percentiles;
	}
	
	@Override
	public Map<String, Double> rangeValues() {
		Map<String, Double> values = new HashMap<String, Double>();
		values.put("min", min());
		values.put("mean", mean());
		values.put("max", max());
		return values;
	}
	
	@Override
	public Map<String, Double> barValues() {
		Map<String, Double> values = new HashMap<String, Double>();
		values.put("count", (double)count());
		return values;
	}
	
}
