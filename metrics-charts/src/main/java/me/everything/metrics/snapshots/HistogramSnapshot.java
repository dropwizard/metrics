package me.everything.metrics.snapshots;

import java.util.HashMap;
import java.util.Map;

import android.util.SparseArray;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;

public class HistogramSnapshot extends MetricSnapshot {

	public static final String MetricType = "Histogram";
	
	public final long count;		
	
	private final transient Snapshot snapshot;
	private final double min, max, mean, stddev, p75, p95, p99;
	private final double p10, p20, p30, p40, p50, p60, p70, p80, p90;

	public HistogramSnapshot(String name, Histogram hist) {
		super(name);

		count = hist.getCount();
		
		snapshot = hist.getSnapshot();
		min = snapshot.getMin();
		max = snapshot.getMax();
		mean = snapshot.getMean();
		stddev = snapshot.getStdDev();
		
		p10 = snapshot.getValue(0.1);
		p20 = snapshot.getValue(0.2);
		p30 = snapshot.getValue(0.3);
		p40 = snapshot.getValue(0.4);
		p50 = snapshot.getMedian();
		p60 = snapshot.getValue(0.6);
		p70 = snapshot.getValue(0.7);
		p80 = snapshot.getValue(0.8);		
		p90 = snapshot.getValue(0.9);
		
		p75 = snapshot.get75thPercentile();
		p95 = snapshot.get95thPercentile();
		p99 = snapshot.get99thPercentile();		
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

	public double p75() {
		return p75;
	}

	public double p95() {
		return p95;
	}
	
	public double p99() {
		return p99;
	}

	public double p10() {
		return p10;
	}

	public double p20() {
		return p20;
	}

	public double p30() {
		return p30;
	}

	public double p40() {
		return p40;
	}

	public double p50() {
		return p50;
	}

	public double p60() {
		return p60;
	}

	public double p70() {
		return p70;
	}

	public double p80() {
		return p80;
	}

	public double p90() {
		return p90;
	}
	
	@Override
	public Map<String, Double> allValues() {
		Map<String, Double> values = new HashMap<String, Double>();
		values.put("count", (double)count());
		values.put("min", min());
		values.put("mean", mean());
		values.put("max", max());
		return values;
	}

	@Override
	public SparseArray<Double> percentileValues() {
		SparseArray<Double> percentiles = new SparseArray<Double>();
		percentiles.put(0, min());
		percentiles.put(10, p10());
		percentiles.put(20, p20());
		percentiles.put(30, p30());
		percentiles.put(40, p40());
		percentiles.put(50, p50());
		percentiles.put(60, p60());
		percentiles.put(70, p70());
		percentiles.put(75, p75());
		percentiles.put(80, p80());
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
