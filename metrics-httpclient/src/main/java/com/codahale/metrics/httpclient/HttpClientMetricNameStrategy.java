package com.codahale.metrics.httpclient;

import com.codahale.metrics.MetricRegistry;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import com.codahale.metrics.MetricName;

@FunctionalInterface
public interface HttpClientMetricNameStrategy {

    MetricName getNameFor(String name, HttpRequest request);

    default MetricName getNameFor(String name, Exception exception) {
        return MetricRegistry.name(HttpClient.class,
                name,
                exception.getClass().getSimpleName());
    }
}
