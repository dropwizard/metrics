package com.yammer.metrics.httpclient;

import com.yammer.metrics.MetricRegistry;
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

import static com.yammer.metrics.httpclient.HttpClientMetricNameStrategies.METHOD_ONLY;

public class InstrumentedHttpClient extends DefaultHttpClient {
    private final Log log = LogFactory.getLog(getClass());

    private final MetricRegistry registry;
    private final String name;
    private final HttpClientMetricNameStrategy metricNameStrategy;

    public InstrumentedHttpClient(MetricRegistry registry,
                                  InstrumentedClientConnManager manager,
                                  HttpParams params,
                                  String name,
                                  HttpClientMetricNameStrategy metricNameStrategy) {
        super(manager, params);
        this.registry = registry;
        this.name = name;
        this.metricNameStrategy = metricNameStrategy;
    }

    public InstrumentedHttpClient(MetricRegistry registry,
                                  InstrumentedClientConnManager manager,
                                  HttpParams params,
                                  String name) {
        this(registry, manager, params, name, METHOD_ONLY);
    }

    public InstrumentedHttpClient(MetricRegistry registry,
                                  HttpParams params) {
        this(registry, new InstrumentedClientConnManager(registry), params, null);
    }

    public InstrumentedHttpClient(MetricRegistry registry) {
        this(registry, new InstrumentedClientConnManager(registry), null, null);
    }

    public InstrumentedHttpClient(MetricRegistry registry, HttpClientMetricNameStrategy metricNameStrategy) {
        this(registry, new InstrumentedClientConnManager(registry), null, null, metricNameStrategy);
    }

    public InstrumentedHttpClient(MetricRegistry registry, String name, HttpClientMetricNameStrategy metricNameStrategy) {
        this(registry, new InstrumentedClientConnManager(registry), null, name, metricNameStrategy);
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
                name,
                metricNameStrategy,
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
                params);
    }
}
