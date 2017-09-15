package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResource {

    @GET
    @Timed
    @Path("/timed")
    public String timed() {
        return "yay";
    }

}
