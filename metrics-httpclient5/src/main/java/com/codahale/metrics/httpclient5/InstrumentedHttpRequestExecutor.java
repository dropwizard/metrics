package com.codahale.metrics.httpclient5;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.io.HttpClientConnection;
import org.apache.hc.core5.http.io.HttpResponseInformationCallback;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;

public class InstrumentedHttpRequestExecutor extends HttpRequestExecutor {
    private final MetricRegistry registry;
    private final HttpClientMetricNameStrategy metricNameStrategy;
    private final String name;

    public InstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpClientMetricNameStrategy metricNameStrategy) {
        this(registry, metricNameStrategy, null);
    }

    public InstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpClientMetricNameStrategy metricNameStrategy,
                                           String name) {
        this(registry, metricNameStrategy, name, HttpRequestExecutor.DEFAULT_WAIT_FOR_CONTINUE);
    }

    public InstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpClientMetricNameStrategy metricNameStrategy,
                                           String name,
                                           Timeout waitForContinue) {
        this(registry, metricNameStrategy, name, waitForContinue, null, null);
    }

    public InstrumentedHttpRequestExecutor(MetricRegistry registry,
                                           HttpClientMetricNameStrategy metricNameStrategy,
                                           String name,
                                           Timeout waitForContinue,
                                           ConnectionReuseStrategy connReuseStrategy,
                                           Http1StreamListener streamListener) {
        super(waitForContinue, connReuseStrategy, streamListener);
        this.registry = registry;
        this.name = name;
        this.metricNameStrategy = metricNameStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, HttpClientConnection conn, HttpResponseInformationCallback informationCallback, HttpContext context) throws IOException, HttpException {
        final Timer.Context timerContext = timer(request).time();
        try {
            return super.execute(request, conn, informationCallback, context);
        } catch (HttpException | IOException e) {
            meter(e).mark();
            throw e;
        } finally {
            timerContext.stop();
        }
    }

    private Timer timer(HttpRequest request) {
        return registry.timer(metricNameStrategy.getNameFor(name, request));
    }

    private Meter meter(Exception e) {
        return registry.meter(metricNameStrategy.getNameFor(name, e));
    }
}
