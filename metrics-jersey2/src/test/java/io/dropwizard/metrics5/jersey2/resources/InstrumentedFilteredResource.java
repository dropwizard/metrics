package io.dropwizard.metrics5.jersey2.resources;

import io.dropwizard.metrics5.annotation.Timed;
import io.dropwizard.metrics5.jersey2.TestClock;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
