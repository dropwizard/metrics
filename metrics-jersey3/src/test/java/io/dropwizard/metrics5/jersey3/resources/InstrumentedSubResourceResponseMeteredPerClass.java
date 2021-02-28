package io.dropwizard.metrics5.jersey3.resources;

import io.dropwizard.metrics5.annotation.ResponseMetered;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ResponseMetered
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResourceResponseMeteredPerClass {
    @GET
    @Path("/responseMeteredPerClass")
    public Response responseMeteredPerClass() {
        return Response.status(Response.Status.OK).build();
    }
}
