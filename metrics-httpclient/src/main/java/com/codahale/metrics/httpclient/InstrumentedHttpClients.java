package com.codahale.metrics.httpclient;

import com.codahale.metrics.MetricRegistry;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import static com.codahale.metrics.httpclient.HttpRequestMetricNameStrategies.METHOD_ONLY;

public class InstrumentedHttpClients {
    private InstrumentedHttpClients() {
        super();
    }

    public static CloseableHttpClient createDefault(MetricRegistry metricRegistry) {
        return createDefault(metricRegistry, METHOD_ONLY);
    }

    public static CloseableHttpClient createDefault(MetricRegistry metricRegistry,
                                                    HttpRequestMetricNameStrategy metricNameStrategy) {
        return custom(metricRegistry, metricNameStrategy).build();
    }

    public static HttpClientBuilder custom(MetricRegistry metricRegistry) {
        return custom(metricRegistry, METHOD_ONLY);
    }

    public static HttpClientBuilder custom(MetricRegistry metricRegistry,
                                           HttpRequestMetricNameStrategy metricNameStrategy) {
        return HttpClientBuilder.create()
                .setRequestExecutor(new InstrumentedHttpRequestExecutor(metricRegistry, metricNameStrategy))
                .setConnectionManager(new InstrumentedHttpClientConnectionManager(metricRegistry));
    }


}
