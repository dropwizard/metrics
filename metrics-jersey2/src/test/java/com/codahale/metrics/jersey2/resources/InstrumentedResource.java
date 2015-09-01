package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
    @Timed(absolute = true, name = "absoluteTimed")
    @Path("/timed/absolute")
    public String timedAbsolute() {
        return "yay";
    }

    @GET
    @Timed(name="namedTimed")
    @Path("/timed/named")
    public String timedNamed() {
        return "yay";
    }

    @GET
    @Metered
    @Path("/metered")
    public String metered() {
        return "woo";
    }

    @GET
    @Metered(absolute = true, name = "absoluteMetered")
    @Path("/metered/absolute")
    public String meteredAbsolute() {
        return "woo";
    }

    @GET
    @Metered(name = "namedMetered")
    @Path("/metered/named")
    public String meteredNamed() {
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
    @ExceptionMetered(cause = IOException.class, absolute = true, name = "absoluteExceptionMetered")
    @Path("/exception-metered/absolute")
    public String absoluteExceptionMetered(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException {
        if (splode) {
            throw new IOException("AUGH");
        }
        return "fuh";
    }

    @GET
    @ExceptionMetered(cause = IOException.class, name = "namedExceptionMeteredOtherName")
    @Path("/exception-metered/named")
    public String namedExceptionMetered(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException {
        if (splode) {
            throw new IOException("AUGH");
        }
        return "fuh";
    }

    @Path("/subresource")
    public InstrumentedSubResource locateSubResource() {
        return new InstrumentedSubResource();
    }
}
