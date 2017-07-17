package com.codahale.metrics.httpclient;

import com.codahale.metrics.MetricRegistry;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;

@FunctionalInterface
public interface HttpClientMetricNameStrategy {

    String getNameFor(String name, HttpRequest request);

    default String getNameFor(String name, Exception exception) {
        return MetricRegistry.name(HttpClient.class,
                name,
                exception.getClass().getSimpleName());
    }
}
