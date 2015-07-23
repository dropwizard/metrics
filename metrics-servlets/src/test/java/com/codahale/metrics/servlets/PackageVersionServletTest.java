package com.codahale.metrics.servlets;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PackageVersionServletTest extends AbstractServletTest {
    @Override
    protected void setUp(ServletTester tester) {
        tester.addServlet(PackageVersionServlet.class, "/package-version");
    }

    @Before
    public void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI("/package-version");
        request.setVersion("HTTP/1.0");

        processRequest();
    }

    @Test
    public void returns200OK() throws Exception {
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    public void returnsNullPackageVersionForUnitTest() throws Exception {
        assertThat(response.getContent())
                .contains("null");
    }

    @Test
    public void returnsTextPlain() throws Exception {
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo("text/plain; charset=ISO-8859-1");
    }

    @Test
    public void returnsUncacheable() throws Exception {
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
                .isEqualTo("must-revalidate,no-cache,no-store");

    }
}
