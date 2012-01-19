package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.MetricsServlet;
import com.yammer.metrics.reporting.MetricsServletContextListener;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import static org.mockito.Mockito.*;

public class MetricsServletContextListenerTest {
    private final MetricsServletContextListener listener = new MetricsServletContextListener();

    @Test
    public void shutsDownAssociatedMetricsRegistry() throws Exception {
        final MetricsRegistry registry = mock(MetricsRegistry.class);

        final ServletContext context = mock(ServletContext.class);
        when(context.getAttribute(MetricsServlet.REGISTRY_ATTRIBUTE)).thenReturn(registry);

        final ServletContextEvent event = mock(ServletContextEvent.class);
        when(event.getServletContext()).thenReturn(context);

        listener.contextDestroyed(event);

        verify(registry).shutdown();
    }

    @Test
    public void isBoringOnStartup() throws Exception {
        final ServletContextEvent event = mock(ServletContextEvent.class);

        listener.contextInitialized(event);

        verifyZeroInteractions(event);
    }
}
