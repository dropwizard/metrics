package com.yammer.metrics.httpclient;

import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.Timer;
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

import static com.yammer.metrics.MetricRegistry.name;

class InstrumentedRequestDirector extends DefaultRequestDirector {
    private final static String GET = "GET", POST = "POST", HEAD = "HEAD", PUT = "PUT",
            OPTIONS = "OPTIONS", DELETE = "DELETE", TRACE = "TRACE",
            CONNECT = "CONNECT", MOVE = "MOVE", PATCH = "PATCH";

    private final Timer getTimer;
    private final Timer postTimer;
    private final Timer headTimer;
    private final Timer putTimer;
    private final Timer deleteTimer;
    private final Timer optionsTimer;
    private final Timer traceTimer;
    private final Timer connectTimer;
    private final Timer moveTimer;
    private final Timer patchTimer;
    private final Timer otherTimer;

    InstrumentedRequestDirector(MetricRegistry registry,
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
        getTimer = registry.timer(name(HttpClient.class, "get-requests"));
        postTimer = registry.timer(name(HttpClient.class, "post-requests"));
        headTimer = registry.timer(name(HttpClient.class, "head-requests"));
        putTimer = registry.timer(name(HttpClient.class, "put-requests"));
        deleteTimer = registry.timer(name(HttpClient.class, "delete-requests"));
        optionsTimer = registry.timer(name(HttpClient.class, "options-requests"));
        traceTimer = registry.timer(name(HttpClient.class, "trace-requests"));
        connectTimer = registry.timer(name(HttpClient.class, "connect-requests"));
        moveTimer = registry.timer(name(HttpClient.class, "move-requests"));
        patchTimer = registry.timer(name(HttpClient.class, "patch-requests"));
        otherTimer = registry.timer(name(HttpClient.class, "other-requests"));
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
        final String method = request.getRequestLine().getMethod();
        if (GET.equalsIgnoreCase(method)) {
            return getTimer;
        } else if (POST.equalsIgnoreCase(method)) {
            return postTimer;
        } else if (PUT.equalsIgnoreCase(method)) {
            return putTimer;
        } else if (HEAD.equalsIgnoreCase(method)) {
            return headTimer;
        } else if (DELETE.equalsIgnoreCase(method)) {
            return deleteTimer;
        } else if (OPTIONS.equalsIgnoreCase(method)) {
            return optionsTimer;
        } else if (TRACE.equalsIgnoreCase(method)) {
            return traceTimer;
        } else if (CONNECT.equalsIgnoreCase(method)) {
            return connectTimer;
        } else if (PATCH.equalsIgnoreCase(method)) {
            return patchTimer;
        } else if (MOVE.equalsIgnoreCase(method)) {
            return moveTimer;
        }
        return otherTimer;
    }
}
