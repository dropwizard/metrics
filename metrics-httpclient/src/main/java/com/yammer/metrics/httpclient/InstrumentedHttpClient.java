package com.yammer.metrics.httpclient;

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

    public InstrumentedHttpClient(InstrumentedClientConnManager manager, HttpParams params) {
        super(manager, params);
    }

    public InstrumentedHttpClient(HttpParams params) {
        super(new InstrumentedClientConnManager(), params);
    }

    public InstrumentedHttpClient() {
        super(new InstrumentedClientConnManager());
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
                                                          AuthenticationHandler targetAuthHandler,
                                                          AuthenticationHandler proxyAuthHandler,
                                                          UserTokenHandler stateHandler,
                                                          HttpParams params) {
        return new InstrumentedRequestDirector(
                log,
                requestExec,
                conman,
                reustrat,
                kastrat,
                rouplan,
                httpProcessor,
                retryHandler,
                redirectStrategy,
                targetAuthHandler,
                proxyAuthHandler,
                stateHandler,
                params);
    }
}
