package io.dropwizard.metrics.httpclient;

import org.apache.http.HttpRequest;

import io.dropwizard.metrics.MetricName;

public interface HttpClientMetricNameStrategy {
    MetricName getNameFor(String name, HttpRequest request);
}
