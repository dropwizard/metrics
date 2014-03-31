package me.everything.metrics.snapshots;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.util.SparseArray;

public abstract class MetricSnapshot {

	private final String name;
	
	private final long timestamp;
	
	public MetricSnapshot(String name) {
		this.name = name;
		this.timestamp = System.currentTimeMillis();
	}

	public String toString() {
		return nameAndType();
	}
	
	public String dataToString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true; 
		for (Map.Entry<String, Double> entry : allValues().entrySet()) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(entry.getKey());
			sb.append(": ");
			sb.append(entry.getValue());
		}
    	return sb.toString();
	}
	
	public String nameAndType() {
		return name + ":" + metricType();
	}
	
	public String name() {
		return name;
	}
	
	public long timestamp() {
		return timestamp;
	}
	
	public Date time() {
		return new Date(timestamp);
	}
	
	public abstract Map<String, Double> allValues();

	public Map<String, Double> rangeValues() {
		return new HashMap<String, Double>();
	}
	
	public Map<String, Double> barValues() {
		return new HashMap<String, Double>();
	}
	
	public SparseArray<Double> percentileValues() {
		return new SparseArray<Double>();
	}
	
	public long count() {
		return 1;
	}
	
	public abstract String metricType();
}
