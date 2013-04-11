package com.yammer.metrics.httpclient;

import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import static com.yammer.metrics.MetricRegistry.name;

public class HttpClientMetricNameStrategies {

    public static final HttpClientMetricNameStrategy METHOD_ONLY = new HttpClientMetricNameStrategy() {
        @Override
        public String getNameFor(String name, HttpRequest request) {
            return name(HttpClient.class,
                        name,
                        methodNameString(request));
        }
    };

    public static final HttpClientMetricNameStrategy HOST_AND_METHOD = new HttpClientMetricNameStrategy() {
        @Override
        public String getNameFor(String name, HttpRequest request) {
            RequestLine requestLine = request.getRequestLine();
            URI uri = URI.create(requestLine.getUri());
            return name(HttpClient.class,
                        name,
                        uri.getHost(),
                        methodNameString(request));
        }
    };

    public static final HttpClientMetricNameStrategy QUERYLESS_URL_AND_METHOD = new HttpClientMetricNameStrategy() {
        @Override
        public String getNameFor(String name, HttpRequest request) {
            try {
                RequestLine requestLine = request.getRequestLine();
                String querylessUrl = new URIBuilder(requestLine.getUri()).removeQuery().build().toString();
                return name(HttpClient.class, name, querylessUrl, methodNameString(request));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    };

    private static String methodNameString(HttpRequest request) {
        return request.getRequestLine().getMethod().toLowerCase() + "-requests";
    }
}
