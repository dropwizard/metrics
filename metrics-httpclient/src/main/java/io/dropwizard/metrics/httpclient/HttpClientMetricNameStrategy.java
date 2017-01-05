package io.dropwizard.metrics.httpclient;

import static io.dropwizard.metrics.MetricRegistry.name;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;

import io.dropwizard.metrics.MetricName;

public interface HttpClientMetricNameStrategy {
    MetricName getNameFor(String name, HttpRequest request);
    default MetricName getNameFor(String name, Exception exception) {
        return name(HttpClient.class,
            name,
            exception.getClass().getSimpleName());
    }
}
