package com.codahale.metrics;

import java.util.Set;

/**
 * A filter used to determine whether or not a metric should be reported, among other things.
 */
public interface MetricFilter {
    /**
     * Matches all metrics, regardless of type or name.
     */
    MetricFilter ALL = (n, m, a) -> true;

    boolean matches(String name, Metric metric, MetricAttribute attribute);

    /**
     * Returns {@code true} if the metric matches the filter; {@code false} otherwise.
     *
     * @param name      the metric's name
     * @param metric    the metric
     * @return {@code true} if the metric matches the filter
     */
    default boolean matches(String name, Metric metric) {
        return matches(name, metric, MetricAttribute.ALL);
    }

    @Deprecated
    static MetricFilter disableMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
        return (name, metric, attribute) -> !disabledMetricAttributes.contains(attribute);
    }

    default MetricFilter and(MetricFilter filter) {
        return (name, metric, attribute) -> this.matches(name, metric, attribute) && filter.matches(name, metric, attribute);
    }

}
