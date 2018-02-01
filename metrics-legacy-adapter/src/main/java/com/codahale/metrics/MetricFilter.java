package com.codahale.metrics;

@Deprecated
public interface MetricFilter {

    MetricFilter ALL = (name, metric) -> true;

    static MetricFilter startsWith(String prefix) {
        return (name, metric) -> name.startsWith(prefix);
    }

    static MetricFilter endsWith(String suffix) {
        return (name, metric) -> name.endsWith(suffix);
    }

    static MetricFilter contains(String substring) {
        return (name, metric) -> name.contains(substring);
    }

    boolean matches(String name, Metric metric);

    default io.dropwizard.metrics5.MetricFilter transform() {
        final MetricFilter origin = this;
        return (name, metric) -> origin.matches(name.getKey(), Metric.of(metric));
    }
}
