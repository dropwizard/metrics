package io.dropwizard.metrics5.jersey2.resources;

import io.dropwizard.metrics5.annotation.ResponseMetered;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ResponseMetered
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResourceResponseMeteredPerClass {
    @GET
    @Path("/responseMeteredPerClass")
    public Response responseMeteredPerClass() {
        return Response.status(Response.Status.OK).build();
    }
}
