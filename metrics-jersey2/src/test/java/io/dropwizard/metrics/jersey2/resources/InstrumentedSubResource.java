package io.dropwizard.metrics.jersey2.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import io.dropwizard.metrics.annotation.Timed;
import io.dropwizard.metrics.jersey2.TestClock;

@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResource {
    private final TestClock testClock;

    public InstrumentedSubResource(TestClock testClock) {
        this.testClock = testClock;
    }

    @GET
    @Timed
    @Path("/timed")
    public String timed() {
        testClock.tick += 2;
        return "yay";
    }

}
