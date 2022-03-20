package com.codahale.metrics.httpclient;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.NameUtility;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;

@FunctionalInterface
public interface HttpClientMetricNameStrategy {

    String getNameFor(String name, HttpRequest request);

    default String getNameFor(String name, Exception exception) {
        return NameUtility.name(HttpClient.class,
                name,
                exception.getClass().getSimpleName());
    }
}
