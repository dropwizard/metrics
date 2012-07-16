package com.yammer.metrics.core;

import java.util.EventListener;

/**
 * Listeners for events from metrics. Listeners must be thread safe. No timeouts
 * are performed by the framework so listeners should also be non-blocking.
 */
public interface MetricListener extends EventListener {

    /**
     * Called to determine what {@link MetricName} and {@link Metric}s the
     * listener wishes to register for. Calls to this method should be
     * consistent throughout the life span of the instance and either the same
     * {@link MetricPredicate} should be returned, or one with the same
     * semantics. Failure to do so will cause removal of this listener to fail
     * on some metrics.
     * 
     * @return the {@link MetricPredicate}
     */
    MetricPredicate getMetricPredicate();
}
