package io.dropwizard.metrics.servlet6;

import com.codahale.metrics.MetricRegistry;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.junit.Test;

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

        verify(context).setAttribute("io.dropwizard.metrics.servlet6.InstrumentedFilter.registry", registry);
    }
}
