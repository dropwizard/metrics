package com.yammer.metrics.util;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;

/**
 * A {@link MetricPredicate} is used to determine whether a metric should be included when sorting
 * and filtering metrics. This is especially useful for limited metric reporting.
 */
public interface MetricPredicate {
    /**
     * A predicate which matches all inputs.
     */
    static final MetricPredicate ALL = new MetricPredicate() {
        @Override
        public boolean matches(MetricName name, Metric metric) {
            return true;
        }
    };

    /**
     * Returns {@code true} if the metric matches the predicate.
     *
     * @param name   the name of the metric
     * @param metric the metric itself
     * @return {@code true} if the predicate applies, {@code false} otherwise
     */
    boolean matches(MetricName name, Metric metric);
}
