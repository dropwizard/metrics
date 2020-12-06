package com.codahale.metrics.jersey3.resources;

import com.codahale.metrics.annotation.Timed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Timed
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResourceTimedPerClass {
    @GET
    @Path("/timedPerClass")
    public String timedPerClass() {
        return "yay";
    }
}
