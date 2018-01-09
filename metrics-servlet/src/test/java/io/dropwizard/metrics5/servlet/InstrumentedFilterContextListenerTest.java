package io.dropwizard.metrics5.servlet;

import io.dropwizard.metrics5.MetricRegistry;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InstrumentedFilterContextListenerTest {
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final InstrumentedFilterContextListener listener = new InstrumentedFilterContextListener() {
        @Override
        protected MetricRegistry getMetricRegistry() {
            return registry;
        }
    };

    @Test
    public void injectsTheMetricRegistryIntoTheServletContext() {
        final ServletContext context = mock(ServletContext.class);

        final ServletContextEvent event = mock(ServletContextEvent.class);
        when(event.getServletContext()).thenReturn(context);

        listener.contextInitialized(event);

        verify(context).setAttribute("io.dropwizard.metrics5.servlet.InstrumentedFilter.registry", registry);
    }
}
