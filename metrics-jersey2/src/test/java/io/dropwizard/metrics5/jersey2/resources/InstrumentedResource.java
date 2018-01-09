package io.dropwizard.metrics5.jersey2.resources;

import io.dropwizard.metrics5.annotation.ExceptionMetered;
import io.dropwizard.metrics5.annotation.Metered;
import io.dropwizard.metrics5.annotation.ResponseMetered;
import io.dropwizard.metrics5.annotation.Timed;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
