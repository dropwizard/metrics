package io.dropwizard.metrics5;

/**
 * A filter used to determine whether or not a metric should be reported, among other things.
 */
public interface MetricFilter {
    /**
     * Matches all metrics, regardless of type or name.
     */
    MetricFilter ALL = (name, metric) -> true;

    static MetricFilter startsWith(String prefix) {
        return (name, metric) -> name.getKey().startsWith(prefix);
    }

    static MetricFilter endsWith(String suffix) {
        return (name, metric) -> name.getKey().endsWith(suffix);
    }

    static MetricFilter contains(String substring) {
        return (name, metric) -> name.getKey().contains(substring);
    }

    /**
     * Returns {@code true} if the metric matches the filter; {@code false} otherwise.
     *
     * @param name   the metric's name
     * @param metric the metric
     * @return {@code true} if the metric matches the filter
     */
    boolean matches(MetricName name, Metric metric);
}
