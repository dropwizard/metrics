package com.codahale.metrics.httpclient;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.IOException;

public class InstrumentedHttpRequestExecutor extends HttpRequestExecutor {
    private final MetricRegistry registry;
    private final HttpRequestMetricNameStrategy metricNameStrategy;
    private final String name;

    public InstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpRequestMetricNameStrategy metricNameStrategy) {
        this(registry, metricNameStrategy, null);
    }

    public InstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpRequestMetricNameStrategy metricNameStrategy,
                                           String name) {
        this(registry, metricNameStrategy, name, HttpRequestExecutor.DEFAULT_WAIT_FOR_CONTINUE);
    }

    public InstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpRequestMetricNameStrategy metricNameStrategy,
                                           String name,
                                           int waitForContinue) {
        super(waitForContinue);
        this.registry = registry;
        this.name = name;
        this.metricNameStrategy = metricNameStrategy;
    }

    @Override
    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context) throws HttpException, IOException {
        final Counter active = registry.counter(metricNameStrategy.getNameForActive(HttpClient.class, name, request));
        final Timer.Context timerContext = registry.timer(metricNameStrategy.getNameForDuration(HttpClient.class, name, request)).time();
        active.inc();
        try {
            return super.execute(request, conn, context);
        } finally {
            active.dec();
            timerContext.stop();
        }
    }

}
