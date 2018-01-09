package io.dropwizard.metrics5.jersey2.exception.mapper;

import io.dropwizard.metrics5.jersey2.exception.TestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class TestExceptionMapper implements ExceptionMapper<TestException> {
    @Override
    public Response toResponse(TestException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
