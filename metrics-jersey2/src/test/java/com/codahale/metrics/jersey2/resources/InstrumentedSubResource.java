package com.codahale.metrics.jersey2.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.jersey2.TestClock;

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
