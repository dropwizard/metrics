package com.codahale.metrics.jersey2;


import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import static com.codahale.metrics.MetricRegistry.name;

public class Jersey2MetricNameStrategies {

    public static final Jersey2MetricNameStrategy METHOD_ONLY =
            new Jersey2MetricNameStrategy() {
                @Override
                public String getNameFor(String name, ClientRequestContext context) {
                    return name(Client.class,
                            name,
                            methodNameString(context));
                }
            };

    public static final Jersey2MetricNameStrategy HOST_AND_METHOD =
            new Jersey2MetricNameStrategy() {
                @Override
                public String getNameFor(String name, ClientRequestContext context) {
                    final URI uri = context.getUri();
                    return name(Client.class,
                            name,
                            uri.getHost(),
                            methodNameString(context));
                }
            };

    public static final Jersey2MetricNameStrategy QUERYLESS_URL_AND_METHOD =
            new Jersey2MetricNameStrategy() {
                @Override
                public String getNameFor(String name, ClientRequestContext context) {
                    final UriBuilder url = UriBuilder.fromUri(context.getUri());
                    return name(Client.class,
                            name,
                            url.replaceQuery("").build().toString(),
                            methodNameString(context));
                }
            };

    private static String methodNameString(ClientRequestContext context) {
        return context.getMethod().toLowerCase() + "-requests";
    }
}