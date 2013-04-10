package com.yammer.metrics.httpclient.strategies;

import com.yammer.metrics.core.MetricName;
import org.apache.http.HttpRequest;

public interface HttpClientMetricNameStrategy {
    MetricName getNameFor(HttpRequest request);
}
