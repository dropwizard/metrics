package io.dropwizard.metrics5.jersey31.resources;

import io.dropwizard.metrics5.annotation.ExceptionMetered;
import io.dropwizard.metrics5.annotation.Metered;
import io.dropwizard.metrics5.annotation.ResponseMetered;
import io.dropwizard.metrics5.annotation.Timed;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;

import static io.dropwizard.metrics5.annotation.ResponseMeteredLevel.ALL;
import static io.dropwizard.metrics5.annotation.ResponseMeteredLevel.COARSE;
import static io.dropwizard.metrics5.annotation.ResponseMeteredLevel.DETAILED;

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
