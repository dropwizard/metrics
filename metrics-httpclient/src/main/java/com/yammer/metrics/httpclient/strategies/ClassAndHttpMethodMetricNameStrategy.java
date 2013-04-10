package com.yammer.metrics.httpclient.strategies;

import com.yammer.metrics.core.MetricName;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;

public class ClassAndHttpMethodMetricNameStrategy implements HttpClientMetricNameStrategy {

    @Override
    public MetricName getNameFor(HttpRequest request) {
        return new MetricName(HttpClient.class,
                              request.getRequestLine().getMethod().toLowerCase() + "-requests");
    }
}
