package com.codahale.metrics.servlets;

import com.codahale.metrics.servlets.AbstractServletTest;
import com.codahale.metrics.servlets.MetricsServlet;
import com.codahale.metrics.servlets.MetricsServletContextListener;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


public class MetricsServletContextListenerTest extends AbstractServletTest {

    @Override
    protected void setUp(ServletTester tester) {
        // removing the EventListener line below reproduces the failure case
        tester.addEventListener(new MetricsServletContextListener());
        tester.addServlet(MetricsServlet.class, "/metrics");
    }
    @Before
    public void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI("/metrics");
        request.setVersion("HTTP/1.0");
    }

    @Test
    public void eventListenerConfiguresRegistry() throws Exception {
        processRequest();
        assertThat(response.getStatus())
                .isEqualTo(200);
    }
}
