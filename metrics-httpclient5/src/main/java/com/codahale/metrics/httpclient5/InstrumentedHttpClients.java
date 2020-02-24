package com.codahale.metrics.httpclient5;

import com.codahale.metrics.MetricRegistry;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

import static com.codahale.metrics.httpclient5.HttpClientMetricNameStrategies.METHOD_ONLY;

public class InstrumentedHttpClients {
    private InstrumentedHttpClients() {
        super();
    }

    public static CloseableHttpClient createDefault(MetricRegistry metricRegistry) {
        return createDefault(metricRegistry, METHOD_ONLY);
    }

    public static CloseableHttpClient createDefault(MetricRegistry metricRegistry,
                                                    HttpClientMetricNameStrategy metricNameStrategy) {
        return custom(metricRegistry, metricNameStrategy).build();
    }

    public static HttpClientBuilder custom(MetricRegistry metricRegistry) {
        return custom(metricRegistry, METHOD_ONLY);
    }

    public static HttpClientBuilder custom(MetricRegistry metricRegistry,
                                           HttpClientMetricNameStrategy metricNameStrategy) {
        return HttpClientBuilder.create()
                .setRequestExecutor(new InstrumentedHttpRequestExecutor(metricRegistry, metricNameStrategy))
                .setConnectionManager(InstrumentedHttpClientConnectionManager.builder(metricRegistry).build());
    }

}
