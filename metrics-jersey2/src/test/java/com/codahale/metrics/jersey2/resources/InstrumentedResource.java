package com.codahale.metrics.jersey2.resources;

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
    @Path("/response-2xx-metered")
    public Response response2xxMetered() {
        return Response.ok().build();
    }

    @GET
    @ResponseMetered
    @Path("/response-4xx-metered")
    public Response response4xxMetered() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @ResponseMetered
    @Path("/response-5xx-metered")
    public Response response5xxMetered() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Path("/subresource")
    public InstrumentedSubResource locateSubResource() {
        return new InstrumentedSubResource();
    }
}
