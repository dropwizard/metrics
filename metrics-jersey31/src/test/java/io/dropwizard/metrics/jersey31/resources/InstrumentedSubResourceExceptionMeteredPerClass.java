package io.dropwizard.metrics.jersey31.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
