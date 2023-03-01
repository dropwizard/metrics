package com.codahale.metrics.jersey3.resources;

import com.codahale.metrics.annotation.ResponseMetered;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static com.codahale.metrics.annotation.ResponseMeteredLevel.ALL;

@ResponseMetered(level = ALL)
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedSubResourceResponseMeteredPerClass {
    @GET
    @Path("/responseMeteredPerClass")
    public Response responseMeteredPerClass() {
        return Response.status(Response.Status.OK).build();
    }
}
