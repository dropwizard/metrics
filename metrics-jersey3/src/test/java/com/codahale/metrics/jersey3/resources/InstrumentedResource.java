package com.codahale.metrics.jersey3.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

import static com.codahale.metrics.annotation.ResponseMeteredLevel.COARSE;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.DETAILED;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.ALL;

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
    @ResponseMetered(level = DETAILED)
    @Path("/response-metered-detailed")
    public Response responseMeteredDetailed(@QueryParam("status_code") @DefaultValue("200") int statusCode) {
        return Response.status(Response.Status.fromStatusCode(statusCode)).build();
    }

    @GET
    @ResponseMetered(level = COARSE)
    @Path("/response-metered-coarse")
    public Response responseMeteredCoarse(@QueryParam("status_code") @DefaultValue("200") int statusCode) {
        return Response.status(Response.Status.fromStatusCode(statusCode)).build();
    }

    @GET
    @ResponseMetered(level = ALL)
    @Path("/response-metered-all")
    public Response responseMeteredAll(@QueryParam("status_code") @DefaultValue("200") int statusCode) {
        return Response.status(Response.Status.fromStatusCode(statusCode)).build();
    }

    @Path("/subresource")
    public InstrumentedSubResource locateSubResource() {
        return new InstrumentedSubResource();
    }
}
