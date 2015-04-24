package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedResource implements InstrumentedResourceInterface {
    @GET
    @Timed
    @Path("/timed")
    public String timed() {
        return "yay";
    }

    @Override
    public String timedInInterface() {
        return "yay-interface";
    }

    @Override
    @Timed
    public String timedInImplementation() {
        return "yay-implementation";
    }

    @GET
    @Metered
    @Path("/metered")
    public String metered() {
        return "woo";
    }

    @Override
    public String meteredInInterface() {
        return "woo-interface";
    }

    @Override
    @Metered
    public String meteredInImplementation() {
        return "woo-implementation";
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

    @Override
    public String exceptionMeteredInInterface(boolean splode) throws IOException {
        if (splode) {
            throw new IOException("AUGH");
        }
        return "fuh-interface";
    }

    @Override
    @ExceptionMetered(cause = IOException.class)
    public String exceptionMeteredInImplementation(boolean splode) throws IOException {
        if (splode) {
            throw new IOException("AUGH");
        }
        return "fuh-implementation";
    }
}
