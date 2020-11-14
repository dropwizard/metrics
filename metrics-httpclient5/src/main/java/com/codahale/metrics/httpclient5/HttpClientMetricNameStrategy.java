package com.codahale.metrics.httpclient5;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.HttpRequest;

@FunctionalInterface
public interface HttpClientMetricNameStrategy {

    MetricName getNameFor(String name, HttpRequest request);

    default MetricName getNameFor(String name, Exception exception) {
        return MetricRegistry.name(HttpClient.class,
                name,
                exception.getClass().getSimpleName());
    }
}
