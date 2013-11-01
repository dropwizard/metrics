package com.codahale.metrics.servlets;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class DefaultAdminServletContextListenerTest {

    @Test
    public void defaultServlets() throws Exception {
        DefaultAdminServletContextListener listener = getDefaultAdminServletContextListener();
        List<? extends AppDiagnosticBaseServlet> actual = listener.diagnostics();

        assertThat(actual).hasSize(5);
        assertThat(actual.get(0)).isInstanceOf(MetricsServlet.class);
        assertThat(actual.get(1)).isInstanceOf(PingServlet.class);
        assertThat(actual.get(2)).isInstanceOf(ThreadDumpServlet.class);
        assertThat(actual.get(3)).isInstanceOf(HealthCheckServlet.class);
        assertThat(actual.get(4)).isInstanceOf(VersionServlet.class);
    }

    private DefaultAdminServletContextListener getDefaultAdminServletContextListener() {
        return new DefaultAdminServletContextListener() {
            @Override
            protected HealthCheckRegistry getHealthCheckRegistry() {
                return null;
            }

            @Override
            protected MetricRegistry getMetricRegistry() {
                return null;
            }
        };
    }
}
