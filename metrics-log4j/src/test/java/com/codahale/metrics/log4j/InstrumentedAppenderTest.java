package com.codahale.metrics.log4j;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstrumentedAppenderTest {

    public static final String METRIC_NAME_PREFIX = "org.apache.log4j.Appender";

    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedAppender appender = new InstrumentedAppender(registry);
    private final LoggingEvent event = mock(LoggingEvent.class);

    @Before
    public void setUp() throws Exception {
        appender.activateOptions();
    }

    @After
    public void tearDown() throws Exception {
        SharedMetricRegistries.clear();
    }

    @Test
    public void metersTraceEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.TRACE);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".trace").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersDebugEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.DEBUG);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".debug").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersInfoEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.INFO);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".info").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersWarnEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.WARN);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".warn").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersErrorEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.ERROR);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".error").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersFatalEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.FATAL);

        appender.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".fatal").getCount())
                .isEqualTo(1);
    }

    @Test
    public void usesSharedRegistries() throws Exception {
        String registryName = "registry";

        SharedMetricRegistries.add(registryName, registry);

        final InstrumentedAppender shared = new InstrumentedAppender(registryName);
        shared.activateOptions();

        when(event.getLevel()).thenReturn(Level.INFO);

        shared.doAppend(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".info").getCount())
                .isEqualTo(1);
    }
}
