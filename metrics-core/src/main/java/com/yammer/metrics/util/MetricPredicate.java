package com.yammer.metrics.util;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;


/**
 * A MetricPredicate is used to determine whether a metric should be included when using
 * sortAndFilterMetrics. This is especially useful for limited metric reporting
 */
public interface MetricPredicate {
    public boolean apply(MetricName name, Metric metric);
}
