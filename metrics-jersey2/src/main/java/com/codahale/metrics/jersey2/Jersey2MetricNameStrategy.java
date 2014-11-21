package com.codahale.metrics.jersey2;

import javax.ws.rs.client.ClientRequestContext;

public interface Jersey2MetricNameStrategy {
    String getNameFor(String name, ClientRequestContext context);
}
