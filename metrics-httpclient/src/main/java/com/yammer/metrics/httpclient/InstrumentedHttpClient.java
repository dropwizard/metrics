package com.yammer.metrics.httpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.httpclient.strategies.ClassAndHttpMethodMetricNameStrategy;
import com.yammer.metrics.httpclient.strategies.HttpClientMetricNameStrategy;
import com.yammer.metrics.httpclient.strategies.MethodOnlyMetricNameStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.client.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

public class InstrumentedHttpClient extends DefaultHttpClient {
    private final Log log = LogFactory.getLog(getClass());

    private final MetricsRegistry registry;
    private final HttpClientMetricNameStrategy metricNameStrategy;

    public InstrumentedHttpClient(MetricsRegistry registry,
                                  InstrumentedClientConnManager manager,
                                  HttpParams params,
                                  HttpClientMetricNameStrategy metricNameStrategy) {
        super(manager, params);
        this.registry = registry;
        this.metricNameStrategy = metricNameStrategy;
    }

    public InstrumentedHttpClient(MetricsRegistry registry,
                                  InstrumentedClientConnManager manager,
                                  HttpParams params) {
        this(registry, manager, params, new ClassAndHttpMethodMetricNameStrategy());
    }

    public InstrumentedHttpClient(InstrumentedClientConnManager manager, HttpParams params) {
        this(Metrics.defaultRegistry(), manager, params);
    }

    public InstrumentedHttpClient(InstrumentedClientConnManager manager,
                                  HttpParams params,
                                  HttpClientMetricNameStrategy metricNameStrategy) {
        this(Metrics.defaultRegistry(), manager, params, metricNameStrategy);
    }

    public InstrumentedHttpClient(HttpParams params) {
        this(new InstrumentedClientConnManager(), params);
    }

    public InstrumentedHttpClient(HttpParams params, HttpClientMetricNameStrategy metricNameStrategy) {
        this(new InstrumentedClientConnManager(), params, metricNameStrategy);
    }

    public InstrumentedHttpClient() {
        this((HttpParams) null);
    }

    public InstrumentedHttpClient(HttpClientMetricNameStrategy metricNameStrategy) {
        this(null, metricNameStrategy);
    }

    @Override
    protected RequestDirector createClientRequestDirector(HttpRequestExecutor requestExec,
                                                          ClientConnectionManager conman,
                                                          ConnectionReuseStrategy reustrat,
                                                          ConnectionKeepAliveStrategy kastrat,
                                                          HttpRoutePlanner rouplan,
                                                          HttpProcessor httpProcessor,
                                                          HttpRequestRetryHandler retryHandler,
                                                          RedirectStrategy redirectStrategy,
                                                          AuthenticationStrategy targetAuthStrategy,
                                                          AuthenticationStrategy proxyAuthStrategy,
                                                          UserTokenHandler userTokenHandler,
                                                          HttpParams params) {
        return new InstrumentedRequestDirector(
                registry,
                log,
                requestExec,
                conman,
                reustrat,
                kastrat,
                rouplan,
                httpProcessor,
                retryHandler,
                redirectStrategy,
                targetAuthStrategy,
                proxyAuthStrategy,
                userTokenHandler,
                params,
                metricNameStrategy);
    }
}
