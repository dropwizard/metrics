package com.codahale.metrics.httpasyncclient;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.httpclient.HttpRequestMetricNameStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.concurrent.Future;


/**
 * An instrumented Closeable Http Async Client this wraps a CloseableHttpAsyncClient.
 */
public class InstrumentedCloseableHttpAsyncClient extends CloseableHttpAsyncClient {
    private final CloseableHttpAsyncClient ac;
    private final MetricRegistry metricRegistry;
    private final HttpRequestMetricNameStrategy metricNameStrategy;
    private final String name;
    private final Meter submitted;

    public InstrumentedCloseableHttpAsyncClient(CloseableHttpAsyncClient ac, MetricRegistry metricRegistry, HttpRequestMetricNameStrategy metricNameStrategy, String name){
        this.ac = ac;
        this.metricRegistry = metricRegistry;
        this.metricNameStrategy = metricNameStrategy;
        this.name = name;
        this.submitted = metricRegistry.meter(MetricRegistry.name(HttpAsyncClient.class, name, "submitted"));
    }

    @Override
    public boolean isRunning() {
        return ac.isRunning();
    }

    @Override
    public void start() {
        ac.start();
    }

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, HttpContext context, FutureCallback<T> responseCallback) {
        submitted.mark();
        InstrumentedHttpAsyncRequest<T> requestResponse = new InstrumentedHttpAsyncRequest(requestProducer, responseConsumer);
        return ac.execute(requestResponse.getProducer(), requestResponse.getConsumer(), context, responseCallback);
    }

    @Override
    public void close() throws IOException {
        ac.close();
    }

    class InstrumentedHttpAsyncRequest<T> {

        private final HttpAsyncRequestProducer producer;
        private final HttpAsyncResponseConsumer<T> consumer;
        private final HttpRequest httpRequest;
        private final Counter active;
        private final Timer duration;
        private Timer.Context context;

        public InstrumentedHttpAsyncRequest(HttpAsyncRequestProducer producer, HttpAsyncResponseConsumer<T> consumer) {
            try {
                httpRequest = producer.generateRequest();
            } catch(IOException exp) {
                throw new RuntimeException(exp);
            } catch(HttpException exp) {
                throw new RuntimeException(exp);
            }
            this.active = metricRegistry.counter(metricNameStrategy.getNameForActive(HttpAsyncClient.class, name, httpRequest));
            this.duration = metricRegistry.timer(metricNameStrategy.getNameForDuration(HttpAsyncClient.class, name, httpRequest));
            this.producer = new InstrumentedHttpAsyncRequestProducer(producer);
            this.consumer = new InstrumentedHttpAsyncResponseConsumer(consumer);
        }

        public HttpAsyncRequestProducer getProducer(){
            return producer;
        }

        public HttpAsyncResponseConsumer<T> getConsumer() {
            return consumer;
        }

        class InstrumentedHttpAsyncRequestProducer implements HttpAsyncRequestProducer {
            private final HttpAsyncRequestProducer producer;

            public InstrumentedHttpAsyncRequestProducer(HttpAsyncRequestProducer producer){
                this.producer = producer;
            }

            @Override
            public HttpHost getTarget() {
                return producer.getTarget();
            }

            @Override
            public HttpRequest generateRequest() throws IOException, HttpException {
                active.inc();
                context = duration.time();
                return httpRequest;
            }

            @Override
            public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
                producer.produceContent(encoder, ioctrl);
            }

            @Override
            public void requestCompleted(final HttpContext c) {
                try {
                    producer.requestCompleted(c);
                } finally {
                    active.dec();
                }
            }

            @Override
            public void resetRequest() throws IOException {
                producer.resetRequest();
            }

            @Override
            public void close() throws IOException {
                producer.close();
            }

            @Override
            public boolean isRepeatable() {
                return producer.isRepeatable();
            }

            @Override
            public void failed(Exception ex) {
                try {
                    producer.failed(ex);
                } finally {
                    active.dec();
                    if(context != null) {
                        context.stop();
                        context = null;
                    }
                }
            }
        }

        class InstrumentedHttpAsyncResponseConsumer<T> implements HttpAsyncResponseConsumer<T> {
            private final HttpAsyncResponseConsumer<T> consumer;

            InstrumentedHttpAsyncResponseConsumer(HttpAsyncResponseConsumer consumer){
                this.consumer = consumer;
            }

            @Override
            public void responseReceived(HttpResponse r) throws IOException, HttpException {
                active.inc();
                consumer.responseReceived(r);
            }

            @Override
            public void consumeContent(ContentDecoder decoder, IOControl ioctrl) throws IOException {
                consumer.consumeContent(decoder, ioctrl);
            }

            @Override
            public void responseCompleted(HttpContext c) {
                try {
                    consumer.responseCompleted(c);
                } finally {
                    context.stop();
                    context = null;
                    active.dec();
                }
            }

            @Override
            public void failed(Exception ex) {
                try {
                    consumer.failed(ex);
                } finally {
                    if(context != null) {
                        context.stop();
                        context = null;
                    }
                    active.dec();
                }
            }

            @Override
            public Exception getException() {
                return consumer.getException();
            }

            @Override
            public T getResult() {
                return consumer.getResult();
            }

            @Override
            public boolean isDone() {
                return consumer.isDone();
            }

            @Override
            public void close() throws IOException {
                consumer.close();
            }

            @Override
            public boolean cancel() {
                try {
                    return consumer.cancel();
                } finally {
                    context.stop();
                    context = null;
                    active.dec();
                }
            }
        }
    }

}
