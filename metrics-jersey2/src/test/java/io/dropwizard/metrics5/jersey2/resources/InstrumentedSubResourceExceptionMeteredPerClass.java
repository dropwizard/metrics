package io.dropwizard.metrics5.jersey2.resources;

import io.dropwizard.metrics5.annotation.ExceptionMetered;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
