package com.codahale.metrics.servlets;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


public class MetricsServletContextListenerTest extends AbstractServletTest {
    @Override
    protected void setUp(ServletTester tester) {
        tester.addEventListener(new MetricsServletContextListener(new MetricRegistry(),
                                                                  new HealthCheckRegistry()));
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
