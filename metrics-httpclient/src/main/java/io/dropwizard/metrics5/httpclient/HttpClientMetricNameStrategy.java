package io.dropwizard.metrics5.httpclient;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;

@FunctionalInterface
public interface HttpClientMetricNameStrategy {

    MetricName getNameFor(String name, HttpRequest request);

    default MetricName getNameFor(String name, Exception exception) {
        return MetricRegistry.name(HttpClient.class,
                name,
                exception.getClass().getSimpleName());
    }
}
