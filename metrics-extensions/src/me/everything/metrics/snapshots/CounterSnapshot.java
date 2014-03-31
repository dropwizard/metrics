package me.everything.metrics.snapshots;

import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Counter;

public class CounterSnapshot extends MetricSnapshot {

	public static final String MetricType = "Counter";
	
	private final long value;
	
	public CounterSnapshot(String name, Counter counter) {
		super(name);
		value = counter.getCount();
	}
	
	@Override
	public String metricType() {
		return MetricType;
	}
	
	public long value() {
		return value;
	}
	
	@Override
	public Map<String, Double> allValues() {
		Map<String, Double> values = new HashMap<String, Double>();
		values.put("value", (double)value());
		return values;
	}

	@Override
	public Map<String, Double> barValues() {
		Map<String, Double> values = new HashMap<String, Double>();
		values.put("value", (double)value());
		return values;
	}


}
