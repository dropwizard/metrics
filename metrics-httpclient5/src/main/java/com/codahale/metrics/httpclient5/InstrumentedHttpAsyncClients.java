package com.codahale.metrics.httpclient5;

import com.codahale.metrics.MetricRegistry;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;

import static com.codahale.metrics.httpclient5.HttpClientMetricNameStrategies.METHOD_ONLY;

public class InstrumentedHttpAsyncClients {
    private InstrumentedHttpAsyncClients() {
        super();
    }

    public static CloseableHttpAsyncClient createDefault(MetricRegistry metricRegistry) {
        return createDefault(metricRegistry, METHOD_ONLY);
    }

    public static CloseableHttpAsyncClient createDefault(MetricRegistry metricRegistry,
                                                         HttpClientMetricNameStrategy metricNameStrategy) {
        return custom(metricRegistry, metricNameStrategy).build();
    }

    public static HttpAsyncClientBuilder custom(MetricRegistry metricRegistry) {
        return custom(metricRegistry, METHOD_ONLY);
    }

    public static HttpAsyncClientBuilder custom(MetricRegistry metricRegistry,
                                                HttpClientMetricNameStrategy metricNameStrategy) {
        return custom(metricRegistry, metricNameStrategy, InstrumentedAsyncClientConnectionManager.builder(metricRegistry).build());
    }

    public static HttpAsyncClientBuilder custom(MetricRegistry metricRegistry,
                                                HttpClientMetricNameStrategy metricNameStrategy,
                                                AsyncClientConnectionManager clientConnectionManager) {
        return HttpAsyncClientBuilder.create()
                .setConnectionManager(clientConnectionManager)
                .addExecInterceptorBefore(ChainElement.CONNECT.name(), "dropwizard-metrics",
                        new InstrumentedAsyncExecChainHandler(metricRegistry, metricNameStrategy));
    }

}
