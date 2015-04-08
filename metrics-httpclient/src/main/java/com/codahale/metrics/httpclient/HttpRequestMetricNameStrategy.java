package com.codahale.metrics.httpclient;

import org.apache.http.HttpRequest;

/**
 * A naming strategy for deciding what to call the metric name.  This could be potentially based on HttpRequest as
 * well as the class of the request type.
 */
public interface HttpRequestMetricNameStrategy {
    String getNameForActive(Class klass, String name, HttpRequest request);
    String getNameForDuration(Class klass, String name, HttpRequest request);
}
