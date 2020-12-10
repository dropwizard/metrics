package io.dropwizard.metrics5.jersey3.resources;

import io.dropwizard.metrics5.annotation.Timed;
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
