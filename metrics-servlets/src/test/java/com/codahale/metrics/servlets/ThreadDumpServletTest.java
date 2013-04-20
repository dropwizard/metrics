package com.codahale.metrics.servlets;

import org.eclipse.jetty.testing.ServletTester;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ThreadDumpServletTest extends AbstractServletTest {
    @Override
    protected void setUp(ServletTester tester) {
        tester.addServlet(ThreadDumpServlet.class, "/threads");
    }

    @Before
    public void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI("/threads");
        request.setVersion("HTTP/1.0");

        processRequest();
    }

    @Test
    public void returns200OK() throws Exception {
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    public void returnsAThreadDump() throws Exception {
        assertThat(response.getContent())
                .contains("Finalizer");
    }

    @Test
    public void returnsTextPlain() throws Exception {
        assertThat(response.getContentType())
                .isEqualTo("text/plain");
    }

    @Test
    public void returnsUncacheable() throws Exception {
        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo("must-revalidate,no-cache,no-store");

    }
}
