package com.codahale.metrics.jersey3.resources;

import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.jersey3.exception.TestException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ResponseMetered
@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class InstrumentedResourceResponseMeteredPerClass {

    @GET
    @Path("/responseMetered2xxPerClass")
    public Response responseMetered2xxPerClass() {
        return Response.ok().build();
    }

    @GET
    @Path("/responseMetered4xxPerClass")
    public Response responseMetered4xxPerClass() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Path("/responseMetered5xxPerClass")
    public Response responseMetered5xxPerClass() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @GET
    @Path("/responseMeteredBadRequestPerClass")
    public String responseMeteredBadRequestPerClass() {
        throw new BadRequestException();
    }

    @GET
    @Path("/responseMeteredRuntimeExceptionPerClass")
    public String responseMeteredRuntimeExceptionPerClass() {
        throw new RuntimeException();
    }

    @GET
    @Path("/responseMeteredTestExceptionPerClass")
    public String responseMeteredTestExceptionPerClass() {
        throw new TestException("test");
    }

    @Path("/subresource")
    public InstrumentedSubResourceResponseMeteredPerClass locateSubResource() {
        return new InstrumentedSubResourceResponseMeteredPerClass();
    }

}
