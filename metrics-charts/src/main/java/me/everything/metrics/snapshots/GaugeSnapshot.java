package me.everything.metrics.snapshots;

import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Gauge;

public class GaugeSnapshot extends MetricSnapshot {

	public static final String MetricType = "Gauge";
	
	private final Object value;
	
	public GaugeSnapshot(String name, Gauge<?> gauge) {
		super(name);
		value = gauge.getValue();
	}
	
	@Override
	public String metricType() {
		return MetricType;
	}
	
	public Object value() {
		return value;
	}

	public boolean isNumeric() {
		return (value instanceof Number);
	}
	
	public double doubleValue() {
		if (isNumeric()) {
			return ((Number)value).doubleValue();
		} else {
			return 0;
		}
	}
	
	@Override
	public Map<String, Double> allValues() {
		Map<String, Double> values = new HashMap<String, Double>();
		values.put("value", doubleValue());
		return values;
	}
	
	@Override
	public Map<String, Double> barValues() {
		Map<String, Double> values = new HashMap<String, Double>();
		values.put("value", doubleValue());
		return values;
	}

}
