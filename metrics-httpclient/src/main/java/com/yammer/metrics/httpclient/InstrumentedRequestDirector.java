package com.yammer.metrics.httpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
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

class InstrumentedRequestDirector extends DefaultRequestDirector {
    private final static String GET = "GET", POST = "POST", HEAD = "HEAD", PUT = "PUT",
            OPTIONS = "OPTIONS", DELETE = "DELETE", TRACE = "TRACE",
            CONNECT = "CONNECT", MOVE = "MOVE", PATCH = "PATCH";

    private static final Timer GET_TIMER = Metrics.newTimer(HttpClient.class, "get-requests");
    private static final Timer POST_TIMER = Metrics.newTimer(HttpClient.class, "post-requests");
    private static final Timer HEAD_TIMER = Metrics.newTimer(HttpClient.class, "head-requests");
    private static final Timer PUT_TIMER = Metrics.newTimer(HttpClient.class, "put-requests");
    private static final Timer DELETE_TIMER = Metrics.newTimer(HttpClient.class, "delete-requests");
    private static final Timer OPTIONS_TIMER = Metrics.newTimer(HttpClient.class,
                                                                "options-requests");
    private static final Timer TRACE_TIMER = Metrics.newTimer(HttpClient.class, "trace-requests");
    private static final Timer CONNECT_TIMER = Metrics.newTimer(HttpClient.class,
                                                                "connect-requests");
    private static final Timer MOVE_TIMER = Metrics.newTimer(HttpClient.class, "move-requests");
    private static final Timer PATCH_TIMER = Metrics.newTimer(HttpClient.class, "patch-requests");
    private static final Timer OTHER_TIMER = Metrics.newTimer(HttpClient.class, "other-requests");

    InstrumentedRequestDirector(Log log,
                                HttpRequestExecutor requestExec,
                                ClientConnectionManager conman,
                                ConnectionReuseStrategy reustrat,
                                ConnectionKeepAliveStrategy kastrat,
                                HttpRoutePlanner rouplan,
                                HttpProcessor httpProcessor,
                                HttpRequestRetryHandler retryHandler,
                                RedirectStrategy redirectStrategy,
                                AuthenticationHandler targetAuthHandler,
                                AuthenticationHandler proxyAuthHandler,
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
              targetAuthHandler,
              proxyAuthHandler,
              userTokenHandler,
              params);
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
        final String method = request.getRequestLine().getMethod();
        if (GET.equalsIgnoreCase(method)) {
            return GET_TIMER;
        } else if (POST.equalsIgnoreCase(method)) {
            return POST_TIMER;
        } else if (PUT.equalsIgnoreCase(method)) {
            return PUT_TIMER;
        } else if (HEAD.equalsIgnoreCase(method)) {
            return HEAD_TIMER;
        } else if (DELETE.equalsIgnoreCase(method)) {
            return DELETE_TIMER;
        } else if (OPTIONS.equalsIgnoreCase(method)) {
            return OPTIONS_TIMER;
        } else if (TRACE.equalsIgnoreCase(method)) {
            return TRACE_TIMER;
        } else if (CONNECT.equalsIgnoreCase(method)) {
            return CONNECT_TIMER;
        } else if (PATCH.equalsIgnoreCase(method)) {
            return PATCH_TIMER;
        } else if (MOVE.equalsIgnoreCase(method)) {
            return MOVE_TIMER;
        }
        return OTHER_TIMER;
    }
}
