package com.codahale.metrics.httpclient5;

import com.codahale.metrics.MetricRegistry;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.HttpRequest;

@FunctionalInterface
public interface HttpClientMetricNameStrategy {

    String getNameFor(String name, HttpRequest request);

    default String getNameFor(String name, Exception exception) {
        return MetricRegistry.name(HttpClient.class,
                name,
                exception.getClass().getSimpleName());
    }
}
