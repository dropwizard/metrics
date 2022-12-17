package io.dropwizard.metrics5.jersey31.resources;

import io.dropwizard.metrics5.annotation.Timed;
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
