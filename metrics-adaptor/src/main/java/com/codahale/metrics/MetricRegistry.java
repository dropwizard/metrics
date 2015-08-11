package com.codahale.metrics;

import java.util.Map;

import io.dropwizard.metrics.MetricName;

public class MetricRegistry {
	final io.dropwizard.metrics.MetricRegistry reg;
	
	public static String name(Class<?> klass, String... names) {
        return io.dropwizard.metrics.MetricRegistry.name(klass.getName(), names).getKey();
    }
	
	public static String name(String name, String... names) {
		return io.dropwizard.metrics.MetricRegistry.name(name, names).getKey();
    }
	
	public MetricRegistry(io.dropwizard.metrics.MetricRegistry reg){
		this.reg = reg;
	}
	public static MetricRegistry of(io.dropwizard.metrics.MetricRegistry reg){
		return new MetricRegistry(reg);
	}
	
	public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {
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
	
	private void registerAll(MetricName prefix, MetricSet metrics) throws IllegalArgumentException {
        if (prefix == null)
            prefix = MetricName.EMPTY;

        for (Map.Entry<String, Metric> entry : metrics.getMetrics().entrySet()) {
            if (entry.getValue() instanceof MetricSet) {
                registerAll(MetricName.join(prefix, MetricName.build(entry.getKey())), (MetricSet) entry.getValue());
            } else {
                reg.register(MetricName.join(prefix, MetricName.build(entry.getKey())), entry.getValue());
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
}
