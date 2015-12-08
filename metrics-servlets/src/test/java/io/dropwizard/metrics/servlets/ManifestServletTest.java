package io.dropwizard.metrics.servlets;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.Before;
import org.junit.Test;

public class ManifestServletTest extends AbstractServletTest {

	@Override
    protected void setUp(ServletTester tester) {
        tester.addServlet(ManifestServlet.class, "/manifest");
        URL url = this.getClass().getResource("/");
		tester.setResourceBase(url.getPath());
    }

    @Before
    public void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI("/manifest");
        request.setVersion("HTTP/1.0");
        processRequest();
    }

    @Test
    public void returns200OK() throws Exception {
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    public void returnsPong() throws Exception {
       assertThat(response.getContent())
                .isEqualTo("line1: test1\nline2: test2\n\n");
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
