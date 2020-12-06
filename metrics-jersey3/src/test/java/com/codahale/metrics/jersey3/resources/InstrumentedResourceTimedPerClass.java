package com.codahale.metrics.jersey3.resources;

import com.codahale.metrics.annotation.Timed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Timed
@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedResourceTimedPerClass {

    @GET
    @Path("/timedPerClass")
    public String timedPerClass() {
        return "yay";
    }

    @Path("/subresource")
    public InstrumentedSubResourceTimedPerClass locateSubResource() {
        return new InstrumentedSubResourceTimedPerClass();
    }

}
