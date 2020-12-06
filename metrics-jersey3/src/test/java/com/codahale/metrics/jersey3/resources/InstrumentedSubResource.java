package com.codahale.metrics.jersey3.resources;

import com.codahale.metrics.annotation.Timed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResource {

    @GET
    @Timed
    @Path("/timed")
    public String timed() {
        return "yay";
    }

}
