package io.dropwizard.metrics5.servlets;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadDumpServletTest extends AbstractServletTest {
    @Override
    protected void setUp(ServletTester tester) {
        tester.addServlet(ThreadDumpServlet.class, "/threads");
    }

    @BeforeEach
    void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI("/threads");
        request.setVersion("HTTP/1.0");

        processRequest();
    }

    @Test
    void returns200OK() {
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    void returnsAThreadDump() {
        assertThat(response.getContent())
                .contains("Finalizer");
    }

    @Test
    void returnsTextPlain() {
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo("text/plain");
    }

    @Test
    void returnsUncacheable() {
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
                .isEqualTo("must-revalidate,no-cache,no-store");

    }
}
