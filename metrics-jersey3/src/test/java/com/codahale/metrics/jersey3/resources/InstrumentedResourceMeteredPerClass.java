package com.codahale.metrics.jersey3.resources;

import com.codahale.metrics.annotation.Metered;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Metered
@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedResourceMeteredPerClass {

    @GET
    @Path("/meteredPerClass")
    public String meteredPerClass() {
        return "yay";
    }

    @Path("/subresource")
    public InstrumentedSubResourceMeteredPerClass locateSubResource() {
        return new InstrumentedSubResourceMeteredPerClass();
    }

}
