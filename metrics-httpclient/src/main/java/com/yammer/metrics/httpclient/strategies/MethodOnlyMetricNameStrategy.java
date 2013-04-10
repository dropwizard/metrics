package com.yammer.metrics.httpclient.strategies;

import com.yammer.metrics.core.MetricName;
import org.apache.http.HttpRequest;

public class MethodOnlyMetricNameStrategy implements HttpClientMetricNameStrategy {

    private final String domain;

    public MethodOnlyMetricNameStrategy(String domain) {
        this.domain = domain;
    }

    public MethodOnlyMetricNameStrategy() {
        this("default");
    }

    @Override
    public MetricName getNameFor(HttpRequest request) {
        return new MetricName(domain, "http-client", request.getRequestLine().getMethod().toUpperCase());
    }
}
