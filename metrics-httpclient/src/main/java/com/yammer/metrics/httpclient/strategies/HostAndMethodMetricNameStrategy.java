package com.yammer.metrics.httpclient.strategies;

import com.yammer.metrics.core.MetricName;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;

import java.net.URI;

public class HostAndMethodMetricNameStrategy implements HttpClientMetricNameStrategy {

    private final String domain;

    public HostAndMethodMetricNameStrategy(String domain) {
        this.domain = domain;
    }

    public HostAndMethodMetricNameStrategy() {
        this("default");
    }

    @Override
    public MetricName getNameFor(HttpRequest request) {
        RequestLine requestLine = request.getRequestLine();
        URI uri = URI.create(requestLine.getUri());

        return new MetricName(domain, uri.getHost(), requestLine.getMethod().toUpperCase());
    }
}
