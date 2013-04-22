package com.codahale.metrics.httpclient;

import org.apache.http.HttpRequest;

public interface HttpClientMetricNameStrategy {
    String getNameFor(String name, HttpRequest request);
}
