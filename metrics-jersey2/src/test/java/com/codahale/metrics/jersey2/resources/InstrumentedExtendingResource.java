package com.codahale.metrics.jersey2.resources;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/concrete")
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedExtendingResource extends InstrumentedAbstractResource {

    @GET
    @Timed
    @Path("concrete")
    public String fromConcreteClass() {
        return "yay";
    }

    @Override
    public String fromAbstractClass() {
        return "concrete";
    }

}
