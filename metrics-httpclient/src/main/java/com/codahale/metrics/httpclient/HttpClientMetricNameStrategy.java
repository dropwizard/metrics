package com.codahale.metrics.httpclient;

import org.apache.http.HttpRequest;

import javax.annotation.Nullable;

public interface HttpClientMetricNameStrategy {
    String getNameFor(@Nullable String name, HttpRequest request);
}
