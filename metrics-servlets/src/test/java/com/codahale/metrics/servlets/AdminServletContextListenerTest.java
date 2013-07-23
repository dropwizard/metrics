package com.codahale.metrics.servlets;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings("deprecation")
public class AdminServletContextListenerTest {
    private final MetricRegistry metricRegistry = mock(MetricRegistry.class);
    private final HealthCheckRegistry healthCheckRegistry = mock(HealthCheckRegistry.class);
    private final ExecutorService executorService = mock(ExecutorService.class);
    private final AdminServletContextListener listener = new AdminServletContextListener() {
        @Override
        protected MetricRegistry getMetricRegistry() {
            return metricRegistry;
        }

        @Override
        protected HealthCheckRegistry getHealthCheckRegistry() {
            return healthCheckRegistry;
        }

        @Override
        protected ExecutorService getExecutorService() {
            return executorService;
        }
    };

    private final ServletContext context = mock(ServletContext.class);
    private final ServletContextEvent event = mock(ServletContextEvent.class);

    @Before
    public void setUp() throws Exception {
        when(event.getServletContext()).thenReturn(context);
    }

    @Test
    public void injectsTheMetricRegistry() throws Exception {
        listener.contextInitialized(event);

        verify(context).setAttribute("com.codahale.metrics.servlets.MetricsServlet.registry", metricRegistry);
    }

    @Test
    public void injectsTheHealthCheckRegistry() throws Exception {
        listener.contextInitialized(event);

        verify(context).setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.registry", healthCheckRegistry);
    }

    @Test
    public void injectsTheHealthCheckExecutor() throws Exception {
        listener.contextInitialized(event);

        verify(context).setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.executor", executorService);
    }
}
