package io.dropwizard.metrics5.jersey2.resources;

import io.dropwizard.metrics5.annotation.ResponseMetered;
import io.dropwizard.metrics5.jersey2.exception.TestException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
