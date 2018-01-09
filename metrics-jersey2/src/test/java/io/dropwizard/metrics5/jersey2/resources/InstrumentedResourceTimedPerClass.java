package io.dropwizard.metrics5.jersey2.resources;

import io.dropwizard.metrics5.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
