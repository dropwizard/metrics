package com.codahale.metrics.jersey.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedResource {
    @GET
    @Timed
    @Path("/timed")
    public String timed() {
        return "yay";
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

    @GET
    @ExceptionMetered
    @Path("/failure-response-metered")
    public Response failureResponseMetered(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException {
        if (splode) {
		return Response.status(BAD_REQUEST).entity("failed").build();
            
        }
        return Response.ok().build();
    }

}
