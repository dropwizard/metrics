package com.codahale.metrics.httpclient;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import static com.codahale.metrics.MetricRegistry.name;

public class HttpClientMetricNameStrategies {

    public static final HttpClientMetricNameStrategy METHOD_ONLY =
        (name, request) -> name(HttpClient.class,
            name,
            methodNameString(request));

    public static final HttpClientMetricNameStrategy HOST_AND_METHOD =
        (name, request) -> name(HttpClient.class,
            name,
            requestURI(request).getHost(),
            methodNameString(request));

    public static final HttpClientMetricNameStrategy PATH_AND_METHOD =
        (name, request) -> {
            final URIBuilder url = new URIBuilder(requestURI(request));
            return name(HttpClient.class,
                name,
                url.getPath(),
                methodNameString(request));
        };

    public static final HttpClientMetricNameStrategy QUERYLESS_URL_AND_METHOD =
        (name, request) -> {
            try {
                final URIBuilder url = new URIBuilder(requestURI(request));
                return name(HttpClient.class,
                    name,
                    url.removeQuery().build().toString(),
                    methodNameString(request));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        };

    private static String methodNameString(HttpRequest request) {
        return request.getRequestLine().getMethod().toLowerCase() + "-requests";
    }

    private static URI requestURI(HttpRequest request) {
        if (request instanceof HttpRequestWrapper)
            return requestURI(((HttpRequestWrapper) request).getOriginal());

        return (request instanceof HttpUriRequest) ?
            ((HttpUriRequest) request).getURI() :
            URI.create(request.getRequestLine().getUri());
    }
}
