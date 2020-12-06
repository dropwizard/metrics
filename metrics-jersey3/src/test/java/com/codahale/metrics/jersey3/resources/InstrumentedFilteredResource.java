package com.codahale.metrics.jersey3.resources;

import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.jersey3.TestClock;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedFilteredResource {

    private final TestClock testClock;

    public InstrumentedFilteredResource(TestClock testClock) {
        this.testClock = testClock;
    }

    @GET
    @Timed
    @Path("/timed")
    public String timed() {
        testClock.tick++;
        return "yay";
    }

    @GET
    @Timed(name = "fancyName")
    @Path("/named")
    public String named() {
        testClock.tick++;
        return "fancy";
    }

    @GET
    @Timed(name = "absolutelyFancy", absolute = true)
    @Path("/absolute")
    public String absolute() {
        testClock.tick++;
        return "absolute";
    }

    @Path("/subresource")
    public InstrumentedFilteredSubResource locateSubResource() {
        return new InstrumentedFilteredSubResource();
    }

    @Produces(MediaType.TEXT_PLAIN)
    public class InstrumentedFilteredSubResource {

        @GET
        @Timed
        @Path("/timed")
        public String timed() {
            testClock.tick += 2;
            return "yay";
        }

    }
}
