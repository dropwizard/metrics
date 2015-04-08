package com.codahale.metrics.httpasyncclient;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpRequestMetricNameStrategy;
import com.codahale.metrics.httpclient.HttpRequestMetricNameStrategies;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

public class InstrumentedNHttpClientBuilder extends HttpAsyncClientBuilder {
    private final MetricRegistry metricRegistry;
    private final String name;
    private final HttpRequestMetricNameStrategy metricNameStrategy;

    public InstrumentedNHttpClientBuilder(MetricRegistry metricRegistry, HttpRequestMetricNameStrategy metricNameStrategy, String name) {
        super();
        this.metricRegistry = metricRegistry;
        this.metricNameStrategy = metricNameStrategy;
        this.name = name;
    }

    public InstrumentedNHttpClientBuilder(MetricRegistry metricRegistry) {
        this(metricRegistry, HttpRequestMetricNameStrategies.METHOD_ONLY, null);
    }

    public InstrumentedNHttpClientBuilder(MetricRegistry metricRegistry, HttpRequestMetricNameStrategy metricNameStrategy) {
        this(metricRegistry, metricNameStrategy, null);
    }

    public InstrumentedNHttpClientBuilder(MetricRegistry metricRegistry, String name) {
        this(metricRegistry, HttpRequestMetricNameStrategies.METHOD_ONLY, name);
    }

    @Override
    public CloseableHttpAsyncClient build() {
        return new InstrumentedCloseableHttpAsyncClient(super.build(), metricRegistry, metricNameStrategy, name);
    }

}
