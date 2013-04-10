package com.yammer.metrics.httpclient.strategies;

import com.yammer.metrics.core.MetricName;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class QuerylessUrlAndMethodMetricNameStrategy implements HttpClientMetricNameStrategy {

    private final String domain;

    public QuerylessUrlAndMethodMetricNameStrategy(String domain) {
        this.domain = domain;
    }

    public QuerylessUrlAndMethodMetricNameStrategy() {
        this("default");
    }

    @Override
    public MetricName getNameFor(HttpRequest request) {
        try {
            RequestLine requestLine = request.getRequestLine();
            String querylessUrl = new URIBuilder(requestLine.getUri()).removeQuery().build().toString();
            return new MetricName(domain, querylessUrl, requestLine.getMethod().toUpperCase());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
