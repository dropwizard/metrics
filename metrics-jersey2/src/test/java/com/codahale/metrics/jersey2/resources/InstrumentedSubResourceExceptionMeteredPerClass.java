package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.ExceptionMetered;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ExceptionMetered(cause = IOException.class)
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResourceExceptionMeteredPerClass {
    @GET
    @Path("/exception-metered")
    public String exceptionMetered(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException {
        if (splode) {
            throw new IOException("AUGH");
        }
        return "fuh";
    }
}
