package com.codahale.metrics;

/**
 * A strategy for Metric prefixes for situations where dimensions of a metric may be dynamic.
 *
 * User: wendel.schultz (swps)
 * Date: 10/3/13
 */
public interface MetricPrefixStrategy {

    public String getMetricPrefix();

}
