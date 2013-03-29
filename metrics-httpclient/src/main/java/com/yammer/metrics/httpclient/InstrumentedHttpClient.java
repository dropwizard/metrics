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

public class InstrumentedHttpClient extends DefaultHttpClient {
    private final Log log = LogFactory.getLog(getClass());

    private final MetricRegistry registry;
    private final String name;

    public InstrumentedHttpClient(MetricRegistry registry,
                                  InstrumentedClientConnManager manager,
                                  HttpParams params,
                                  String name) {
        super(manager, params);
        this.registry = registry;
        this.name = name;
    }

    public InstrumentedHttpClient(MetricRegistry registry,
                                  HttpParams params) {
        this(registry, new InstrumentedClientConnManager(registry), params, null);
    }

    public InstrumentedHttpClient(MetricRegistry registry) {
        this(registry, new InstrumentedClientConnManager(registry), null, null);
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
