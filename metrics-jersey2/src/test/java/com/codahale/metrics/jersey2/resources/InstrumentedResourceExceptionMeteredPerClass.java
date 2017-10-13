package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.ExceptionMetered;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ExceptionMetered(cause = IOException.class)
@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedResourceExceptionMeteredPerClass {

    @GET
    @Path("/exception-metered")
    public String exceptionMetered(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException {
        if (splode) {
            throw new IOException("AUGH");
        }
        return "fuh";
    }

    @Path("/subresource")
    public InstrumentedSubResourceExceptionMeteredPerClass locateSubResource() {
        return new InstrumentedSubResourceExceptionMeteredPerClass();
    }

}
