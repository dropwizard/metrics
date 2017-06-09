package com.codahale.metrics.httpasyncclient;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import static java.util.Objects.requireNonNull;

public class InstrumentedNHttpClientBuilder extends HttpAsyncClientBuilder {
    private final MetricRegistry metricRegistry;
    private final String name;
    private final HttpClientMetricNameStrategy metricNameStrategy;

    public InstrumentedNHttpClientBuilder(MetricRegistry metricRegistry, HttpClientMetricNameStrategy metricNameStrategy, String name) {
        super();
        this.metricRegistry = metricRegistry;
        this.metricNameStrategy = metricNameStrategy;
        this.name = name;
    }

    public InstrumentedNHttpClientBuilder(MetricRegistry metricRegistry) {
        this(metricRegistry, HttpClientMetricNameStrategies.METHOD_ONLY, null);
    }

    public InstrumentedNHttpClientBuilder(MetricRegistry metricRegistry, HttpClientMetricNameStrategy metricNameStrategy) {
        this(metricRegistry, metricNameStrategy, null);
    }

    public InstrumentedNHttpClientBuilder(MetricRegistry metricRegistry, String name) {
        this(metricRegistry, HttpClientMetricNameStrategies.METHOD_ONLY, name);
    }

    private Timer timer(HttpRequest request) {
        return metricRegistry.timer(metricNameStrategy.getNameFor(name, request));
    }

    @Override
    public CloseableHttpAsyncClient build() {
        final CloseableHttpAsyncClient ac = super.build();
        return new CloseableHttpAsyncClient() {

            @Override
            public boolean isRunning() {
                return ac.isRunning();
            }

            @Override
            public void start() {
                ac.start();
            }

            @Override
            public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, HttpContext context, FutureCallback<T> callback) {
                final Timer.Context timerContext;
                try {
                    timerContext = timer(requestProducer.generateRequest()).time();
                } catch (IOException | HttpException ex) {
                    throw new RuntimeException(ex);
                }
                return ac.execute(requestProducer, responseConsumer, context,
                        new TimingFutureCallback<>(callback, timerContext));
            }

            @Override
            public void close() throws IOException {
                ac.close();
            }
        };
    }

    private static class TimingFutureCallback<T> implements FutureCallback<T> {
        private final FutureCallback<T> callback;
        private final Timer.Context timerContext;

        private TimingFutureCallback(FutureCallback<T> callback,
                                     Timer.Context timerContext) {
            this.callback = callback;
            this.timerContext = requireNonNull(timerContext, "timerContext");
        }

        @Override
        public void completed(T result) {
            timerContext.stop();
            if(callback != null) {
                callback.completed(result);
            }
        }

        @Override
        public void failed(Exception ex) {
            timerContext.stop();
            if(callback != null) {
                callback.failed(ex);
            }
        }

        @Override
        public void cancelled() {
            timerContext.stop();
            if(callback != null) {
                callback.cancelled();
            }
        }
    }

}
