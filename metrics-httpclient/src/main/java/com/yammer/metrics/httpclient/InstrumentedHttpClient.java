package com.yammer.metrics.httpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.core.TimerMetric;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class InstrumentedHttpClient extends DefaultHttpClient {
    private final static String GET = "GET", POST = "POST", HEAD = "HEAD", PUT = "PUT",
                                OPTIONS = "OPTIONS", DELETE = "DELETE", TRACE = "TRACE",
                                CONNECT = "CONNECT", MOVE = "MOVE", PATCH = "PATCH";

    private static final TimerMetric GET_TIMER = Metrics.newTimer(HttpClient.class, "get-requests");
    private static final TimerMetric POST_TIMER = Metrics.newTimer(HttpClient.class, "post-requests");
    private static final TimerMetric HEAD_TIMER = Metrics.newTimer(HttpClient.class, "head-requests");
    private static final TimerMetric PUT_TIMER = Metrics.newTimer(HttpClient.class, "put-requests");
    private static final TimerMetric DELETE_TIMER = Metrics.newTimer(HttpClient.class, "delete-requests");
    private static final TimerMetric OPTIONS_TIMER = Metrics.newTimer(HttpClient.class, "options-requests");
    private static final TimerMetric TRACE_TIMER = Metrics.newTimer(HttpClient.class, "trace-requests");
    private static final TimerMetric CONNECT_TIMER = Metrics.newTimer(HttpClient.class, "connect-requests");
    private static final TimerMetric MOVE_TIMER = Metrics.newTimer(HttpClient.class, "move-requests");
    private static final TimerMetric PATCH_TIMER = Metrics.newTimer(HttpClient.class, "patch-requests");
    private static final TimerMetric OTHER_TIMER = Metrics.newTimer(HttpClient.class, "other-requests");

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
    public <T> T execute(HttpUriRequest request,
                         ResponseHandler<? extends T> responseHandler) throws IOException {
        final TimerContext context = timer(request).time();
        try {
            return super.execute(request, responseHandler);
        } finally {
            context.stop();
        }
    }

    @Override
    public <T> T execute(HttpUriRequest request,
                         ResponseHandler<? extends T> responseHandler,
                         HttpContext ctxt) throws IOException {
        final TimerContext context = timer(request).time();
        try {
            return super.execute(request, responseHandler, ctxt);
        } finally {
            context.stop();
        }
    }

    @Override
    public <T> T execute(HttpHost target,
                         HttpRequest request,
                         ResponseHandler<? extends T> responseHandler) throws IOException {
        final TimerContext context = timer(request).time();
        try {
            return super.execute(target, request, responseHandler);
        } finally {
            context.stop();
        }
    }

    @Override
    public <T> T execute(HttpHost target,
                         HttpRequest request,
                         ResponseHandler<? extends T> responseHandler,
                         HttpContext ctxt) throws IOException {
        final TimerContext context = timer(request).time();
        try {
            return super.execute(target, request, responseHandler, ctxt);
        } finally {
            context.stop();
        }

    }

    private TimerMetric timer(HttpRequest request) {
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
