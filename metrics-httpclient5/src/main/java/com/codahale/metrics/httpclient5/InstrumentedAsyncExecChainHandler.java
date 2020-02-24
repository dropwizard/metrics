package com.codahale.metrics.httpclient5;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

class InstrumentedAsyncExecChainHandler implements AsyncExecChainHandler {
    private final MetricRegistry registry;
    private final HttpClientMetricNameStrategy metricNameStrategy;
    private final String name;

    public InstrumentedAsyncExecChainHandler(MetricRegistry registry, HttpClientMetricNameStrategy metricNameStrategy) {
        this(registry, metricNameStrategy, null);
    }

    public InstrumentedAsyncExecChainHandler(MetricRegistry registry,
                                             HttpClientMetricNameStrategy metricNameStrategy,
                                             String name) {
        this.registry = requireNonNull(registry, "registry");
        this.metricNameStrategy = requireNonNull(metricNameStrategy, "metricNameStrategy");
        this.name = name;
    }

    @Override
    public void execute(HttpRequest request,
                        AsyncEntityProducer entityProducer,
                        AsyncExecChain.Scope scope,
                        AsyncExecChain chain,
                        AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        final InstrumentedAsyncExecCallback instrumentedAsyncExecCallback =
                new InstrumentedAsyncExecCallback(registry, metricNameStrategy, name, asyncExecCallback, request);
        chain.proceed(request, entityProducer, scope, instrumentedAsyncExecCallback);

    }

    final static class InstrumentedAsyncExecCallback implements AsyncExecCallback {
        private final MetricRegistry registry;
        private final HttpClientMetricNameStrategy metricNameStrategy;
        private final String name;
        private final AsyncExecCallback delegate;
        private final Timer.Context timerContext;

        public InstrumentedAsyncExecCallback(MetricRegistry registry,
                                             HttpClientMetricNameStrategy metricNameStrategy,
                                             String name,
                                             AsyncExecCallback delegate,
                                             HttpRequest request) {
            this.registry = registry;
            this.metricNameStrategy = metricNameStrategy;
            this.name = name;
            this.delegate = delegate;
            this.timerContext = timer(request).time();
        }

        @Override
        public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
            return delegate.handleResponse(response, entityDetails);
        }

        @Override
        public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
            delegate.handleInformationResponse(response);
        }

        @Override
        public void completed() {
            delegate.completed();
            timerContext.stop();
        }

        @Override
        public void failed(Exception cause) {
            delegate.failed(cause);
            meter(cause).mark();
            timerContext.stop();
        }

        private Timer timer(HttpRequest request) {
            return registry.timer(metricNameStrategy.getNameFor(name, request));
        }

        private Meter meter(Exception e) {
            return registry.meter(metricNameStrategy.getNameFor(name, e));
        }
    }
}
