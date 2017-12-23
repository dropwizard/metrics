package com.codahale.metrics.servlets;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.Before;
import org.junit.Test;

public class CpuProfileServletTest extends AbstractServletTest {

    @Override
    protected void setUp(ServletTester tester) {
        tester.addServlet(CpuProfileServlet.class, "/pprof");
    }

    @Before
    public void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI("/pprof?duration=1");
        request.setVersion("HTTP/1.0");

        processRequest();
    }

    @Test
    public void returns200OK() {
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    public void returnsPprofRaw() {
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo("pprof/raw");
    }

    @Test
    public void returnsUncacheable() {
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
                .isEqualTo("must-revalidate,no-cache,no-store");

    }
}
