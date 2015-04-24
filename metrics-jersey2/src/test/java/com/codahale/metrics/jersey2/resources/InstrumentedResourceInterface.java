package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;

public interface InstrumentedResourceInterface {
    @GET
    @Timed
    @Path("/timed-in-interface")
    String timedInInterface();

    @GET
    @Path("/timed-in-implementation")
    String timedInImplementation();

    @GET
    @Metered
    @Path("/metered-in-interface")
    String meteredInInterface();

    @GET
    @Path("/metered-in-implementation")
    String meteredInImplementation();

    @GET
    @ExceptionMetered(cause = IOException.class)
    @Path("/exception-metered-in-interface")
    String exceptionMeteredInInterface(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException;

    @GET
    @Path("/exception-metered-in-implementation")
    String exceptionMeteredInImplementation(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException;
}
