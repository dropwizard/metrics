package io.dropwizard.metrics5.servlets;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CpuProfileServletTest extends AbstractServletTest {

    @Override
    protected void setUp(ServletTester tester) {
        tester.addServlet(CpuProfileServlet.class, "/pprof");
    }

    @BeforeEach
    void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI("/pprof?duration=1");
        request.setVersion("HTTP/1.0");

        processRequest();
    }

    @Test
    void returns200OK() {
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    void returnsPprofRaw() {
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo("pprof/raw");
    }

    @Test
    void returnsUncacheable() {
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
                .isEqualTo("must-revalidate,no-cache,no-store");

    }
}
