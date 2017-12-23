package com.codahale.metrics.servlets;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void returns200OK() {
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    public void returnsAThreadDump() {
        assertThat(response.getContent())
                .contains("Finalizer");
    }

    @Test
    public void returnsTextPlain() {
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo("text/plain");
    }

    @Test
    public void returnsUncacheable() {
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
                .isEqualTo("must-revalidate,no-cache,no-store");

    }
}
