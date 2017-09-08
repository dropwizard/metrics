package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.jersey2.TestClock;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedResource {
    private final TestClock testClock;

    public InstrumentedResource(TestClock testClock) {
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
    @Timed(name="fancyName")
    @Path("/named")
    public String named() {
        testClock.tick++;
        return "fancy";
    }

    @GET
    @Timed(name="absolutelyFancy", absolute = true)
    @Path("/absolute")
    public String absolute() {
        testClock.tick++;
        return "absolute";
    }

    @GET
    @Metered
    @Path("/metered")
    public String metered() {
        return "woo";
    }

    @GET
    @ExceptionMetered(cause = IOException.class)
    @Path("/exception-metered")
    public String exceptionMetered(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException {
        if (splode) {
            throw new IOException("AUGH");
        }
        return "fuh";
    }

    @Path("/subresource")
    public InstrumentedSubResource locateSubResource() {
        return new InstrumentedSubResource(testClock);
    }
}
