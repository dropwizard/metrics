package com.codahale.metrics.jersey2.exception.mapper;

import com.codahale.metrics.jersey2.exception.TestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class TestExceptionMapper implements ExceptionMapper<TestException> {
    public Response toResponse(TestException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
