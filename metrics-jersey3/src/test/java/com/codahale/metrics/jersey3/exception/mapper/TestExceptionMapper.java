package com.codahale.metrics.jersey3.exception.mapper;

import com.codahale.metrics.jersey3.exception.TestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TestExceptionMapper implements ExceptionMapper<TestException> {
    @Override
    public Response toResponse(TestException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
