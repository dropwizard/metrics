package com.codahale.metrics.httpclient;

import org.apache.http.HttpRequest;

import com.codahale.metrics.MetricName;

public interface HttpClientMetricNameStrategy {
    MetricName getNameFor(String name, HttpRequest request);
}
