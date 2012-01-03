package com.yammer.metrics.core;

/**
 * A tag interface to indicate that a class is a metric.
 */
public interface Metric {
    /**
     * Allow the given {@link MetricProcessor} to process {@code this} as a metric.
     *
     * @param processor    a {@link MetricProcessor}
     * @param name         the name of the current metric
     * @param context      a given context which should be passed on to {@code processor}
     * @param <T>          the type of the context object
     * @throws Exception if something goes wrong
     */
    <T> void processWith(MetricProcessor<T> processor, MetricName name, T context) throws Exception;
}
