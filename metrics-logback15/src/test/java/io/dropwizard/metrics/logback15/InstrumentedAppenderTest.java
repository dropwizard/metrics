package io.dropwizard.metrics.logback15;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstrumentedAppenderTest {

    public static final String METRIC_NAME_PREFIX = "ch.qos.logback.core.Appender";

    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedAppender appender = new InstrumentedAppender(registry);
    private final ILoggingEvent event = mock(ILoggingEvent.class);

    @Before
    public void setUp() {
        appender.start();
    }

    @After
    public void tearDown() {
        SharedMetricRegistries.clear();
    }

    @Test
    public void metersTraceEvents() {
        when(event.getLevel()).thenReturn(Level.TRACE);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".trace").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersDebugEvents() {
        when(event.getLevel()).thenReturn(Level.DEBUG);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".debug").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersInfoEvents() {
        when(event.getLevel()).thenReturn(Level.INFO);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".info").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersWarnEvents() {
        when(event.getLevel()).thenReturn(Level.WARN);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".warn").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersErrorEvents() {
        when(event.getLevel()).thenReturn(Level.ERROR);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".error").getCount())
                .isEqualTo(1);
    }

    @Test
    public void usesSharedRegistries() {

        String registryName = "registry";

        SharedMetricRegistries.add(registryName, registry);
        final InstrumentedAppender shared = new InstrumentedAppender(registryName);
        shared.start();

        when(event.getLevel()).thenReturn(Level.INFO);

        shared.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".info").getCount())
                .isEqualTo(1);
    }

    @Test
    public void usesDefaultRegistry() {
        SharedMetricRegistries.add(InstrumentedAppender.DEFAULT_REGISTRY, registry);
        final InstrumentedAppender shared = new InstrumentedAppender();
        shared.start();
        when(event.getLevel()).thenReturn(Level.INFO);
        shared.doAppend(event);

        assertThat(SharedMetricRegistries.names()).contains(InstrumentedAppender.DEFAULT_REGISTRY);
        assertThat(registry.meter(METRIC_NAME_PREFIX + ".info").getCount())
                .isEqualTo(1);
    }

    @Test
    public void usesRegistryFromProperty() {
        SharedMetricRegistries.add("something_else", registry);
        System.setProperty(InstrumentedAppender.REGISTRY_PROPERTY_NAME, "something_else");
        final InstrumentedAppender shared = new InstrumentedAppender();
        shared.start();
        when(event.getLevel()).thenReturn(Level.INFO);
        shared.doAppend(event);

        assertThat(SharedMetricRegistries.names()).contains("something_else");
        assertThat(registry.meter(METRIC_NAME_PREFIX + ".info").getCount())
                .isEqualTo(1);
    }

}
