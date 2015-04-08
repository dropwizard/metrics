package com.codahale.metrics.httpclient;

import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import static com.codahale.metrics.MetricRegistry.name;

public class HttpRequestMetricNameStrategies {

    public static final HttpRequestMetricNameStrategy METHOD_ONLY =
            new HttpRequestMetricNameStrategy() {
                @Override
                public String getNameForActive(Class klass, String name, HttpRequest request) {
                    return name(klass,
                                name,
                                methodNameString(request),
                                "active"
                            );
                }
                @Override
                public String getNameForDuration(Class klass, String name, HttpRequest request) {
                    return name(klass,
                            name,
                            methodNameString(request),
                            "duration");
                }
            };

    public static final HttpRequestMetricNameStrategy HOST_AND_METHOD =
            new HttpRequestMetricNameStrategy() {
                @Override
                public String getNameForActive(Class klass, String name, HttpRequest request) {
                    final RequestLine requestLine = request.getRequestLine();
                    final URI uri = URI.create(requestLine.getUri());
                    return name(klass,
                                name,
                                uri.getHost(),
                                methodNameString(request),
                                "active");
                }
                @Override
                public String getNameForDuration(Class klass, String name, HttpRequest request) {
                    final RequestLine requestLine = request.getRequestLine();
                    final URI uri = URI.create(requestLine.getUri());
                    return name(klass,
                            name,
                            uri.getHost(),
                            methodNameString(request),
                            "duration");
                }
            };

    public static final HttpRequestMetricNameStrategy QUERYLESS_URL_AND_METHOD =
            new HttpRequestMetricNameStrategy() {
                @Override
                public String getNameForActive(Class klass, String name, HttpRequest request) {
                    try {
                        final RequestLine requestLine = request.getRequestLine();
                        final URIBuilder url = new URIBuilder(requestLine.getUri());
                        return name(klass,
                                    name,
                                    url.removeQuery().build().toString(),
                                    methodNameString(request),
                                "active");
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
                @Override
                public String getNameForDuration(Class klass, String name, HttpRequest request) {
                    try {
                        final RequestLine requestLine = request.getRequestLine();
                        final URIBuilder url = new URIBuilder(requestLine.getUri());
                        return name(klass,
                                name,
                                url.removeQuery().build().toString(),
                                methodNameString(request),
                                "duration");
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            };

    private static String methodNameString(HttpRequest request) {
        return request.getRequestLine().getMethod().toLowerCase();
    }
}
