package io.dropwizard.metrics5.jersey2.resources;

import io.dropwizard.metrics5.annotation.Timed;

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
