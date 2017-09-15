package com.codahale.metrics.jersey2.exception;

public class TestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TestException(String message) {
        super(message);
    }
}
