package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResource {
    @GET
    @Timed
    @Path("/timed")
    public String timed() {
        return "yay";
    }

}
