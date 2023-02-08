package io.dropwizard.metrics5.servlets;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PingServletTest extends AbstractServletTest {
    @Override
    protected void setUp(ServletTester tester) {
        tester.addServlet(PingServlet.class, "/ping");
    }

    @BeforeEach
    void setUp() throws Exception  {
        request.setMethod("GET");
        request.setURI("/ping");
        request.setVersion("HTTP/1.0");

        processRequest();
    }

    @Test
    void returns200OK()  {
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    void returnsPong()  {
        assertThat(response.getContent())
                .isEqualTo(String.format("pong%n"));
    }

    @Test
    void returnsTextPlain()  {
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo("text/plain;charset=ISO-8859-1");
    }

    @Test
    void returnsUncacheable()  {
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
                .isEqualTo("must-revalidate,no-cache,no-store");

    }
}
