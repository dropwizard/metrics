package com.codahale.metrics.jersey.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

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
    @ResponseMetered
    @Path("/response-metered-2xx")
    public Response responseMetered2xx() {
        return Response.ok().build();
    }

    @GET
    @ResponseMetered
    @Path("/response-metered-3xx")
    public Response responseMetered3xx() {
        return Response.notModified().build();
    }

    @GET
    @ResponseMetered
    @Path("/response-metered-5xx")
    public Response responseMetered5xx() {
        return Response.serverError().build();
    }

    @GET
    @ResponseMetered
    @Path("/response-metered-io-exception")
    public Response responseMeteredIOException() throws IOException {
        throw new IOException("AUGH");
    }


}
