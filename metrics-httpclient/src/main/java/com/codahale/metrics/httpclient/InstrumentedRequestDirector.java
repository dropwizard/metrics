package com.codahale.metrics.httpclient;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.commons.logging.Log;
import org.apache.http.*;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.IOException;

public class InstrumentedRequestDirector extends DefaultRequestDirector {
    private final MetricRegistry registry;
    private final HttpClientMetricNameStrategy metricNameStrategy;
    private final String name;

    public InstrumentedRequestDirector(MetricRegistry registry,
                                       String name,
                                       HttpClientMetricNameStrategy metricNameStrategy,
                                       Log log,
                                       HttpRequestExecutor requestExec,
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
        super(log,
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
        this.registry = registry;
        this.name = name;
        this.metricNameStrategy = metricNameStrategy;
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException, IOException {
        final Timer.Context timerContext = timer(request).time();
        try {
            return super.execute(target, request, context);
        } finally {
            timerContext.stop();
        }
    }

    private Timer timer(HttpRequest request) {
        return registry.timer(metricNameStrategy.getNameFor(name, request));
    }
}
