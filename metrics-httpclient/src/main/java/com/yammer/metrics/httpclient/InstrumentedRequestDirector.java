package com.yammer.metrics.httpclient;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.httpclient.strategies.HttpClientMetricNameStrategy;
import org.apache.commons.logging.Log;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class InstrumentedRequestDirector extends DefaultRequestDirector {
    private final HttpClientMetricNameStrategy metricNameStrategy;
    private final MetricsRegistry registry;

    InstrumentedRequestDirector(MetricsRegistry registry,
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
                                HttpParams params,
                                HttpClientMetricNameStrategy metricNameStrategy) {
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
        this.metricNameStrategy = metricNameStrategy;
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException, IOException {
        final TimerContext timerContext = timer(request).time();
        try {
            return super.execute(target, request, context);
        } finally {
            timerContext.stop();
        }
    }

    private Timer timer(HttpRequest request) {
        return registry.newTimer(metricNameStrategy.getNameFor(request),
                                        TimeUnit.MILLISECONDS,
                                        TimeUnit.SECONDS);
    }
}
