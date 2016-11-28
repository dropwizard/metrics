package io.dropwizard.metrics.jersey2.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import io.dropwizard.metrics.annotation.Timed;

@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResource {
    @GET
    @Timed
    @Path("/timed")
    public String timed() {
        return "yay";
    }

}
