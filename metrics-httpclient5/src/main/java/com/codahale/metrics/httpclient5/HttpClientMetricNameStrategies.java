package com.codahale.metrics.httpclient5;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;
import java.util.Locale;

import static com.codahale.metrics.MetricRegistry.name;

public class HttpClientMetricNameStrategies {

    public static final HttpClientMetricNameStrategy METHOD_ONLY =
            (name, request) -> name(HttpClient.class,
                    name,
                    methodNameString(request));

    public static final HttpClientMetricNameStrategy HOST_AND_METHOD =
            (name, request) -> {
                try {
                    return name(HttpClient.class,
                            name,
                            request.getUri().getHost(),
                            methodNameString(request));
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
            };

    public static final HttpClientMetricNameStrategy QUERYLESS_URL_AND_METHOD =
            (name, request) -> {
                try {
                    final URIBuilder url = new URIBuilder(request.getUri());
                    return name(HttpClient.class,
                            name,
                            url.removeQuery().build().toString(),
                            methodNameString(request));
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
            };

    private static String methodNameString(HttpRequest request) {
        return request.getMethod().toLowerCase(Locale.ROOT) + "-requests";
    }

}
