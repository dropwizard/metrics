package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/abstract")
@Produces(MediaType.TEXT_PLAIN)
public abstract class InstrumentedAbstractResource implements InstrumentedInterfaceResource {

    @GET
    @Timed
    @Path("abstract")
    public String fromAbstractClass() {
        return "yay";
    }

    @Override
    public String fromInterface() {
        return "abstract";
    }

}
