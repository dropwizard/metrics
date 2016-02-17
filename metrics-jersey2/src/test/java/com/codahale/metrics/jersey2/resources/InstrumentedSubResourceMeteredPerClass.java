package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.Metered;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Metered
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResourceMeteredPerClass {
    @GET
    @Path("/meteredPerClass")
    public String meteredPerClass() {
        return "yay";
    }
}
