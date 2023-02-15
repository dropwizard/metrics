package io.dropwizard.metrics.jersey31.resources;

import io.dropwizard.metrics.jersey31.TestClock;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

import java.io.IOException;

public class TestRequestFilter implements ContainerRequestFilter {

    private final TestClock testClock;

    public TestRequestFilter(TestClock testClock) {
        this.testClock = testClock;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        testClock.tick += 4;
    }
}
