package com.codahale.metrics.log4j;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstrumentedAppenderTest {
    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedAppender appender = new InstrumentedAppender(registry);
    private final LoggingEvent event = mock(LoggingEvent.class);

    @Before
    public void setUp() throws Exception {
        appender.activateOptions();

        when(event.getLevel()).thenReturn(Level.INFO);
    }

    @After
    public void tearDown() throws Exception {
        SharedMetricRegistries.clear();
    }

    @Test
    public void metersTraceEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.TRACE);
        appender.doAppend(event);

        assertThat(registry.meter("org.apache.log4j.Appender.all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter("org.apache.log4j.Appender.trace").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersDebugEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.DEBUG);
        appender.doAppend(event);

        assertThat(registry.meter("org.apache.log4j.Appender.all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter("org.apache.log4j.Appender.debug").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersInfoEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.INFO);
        appender.doAppend(event);

        assertThat(registry.meter("org.apache.log4j.Appender.all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter("org.apache.log4j.Appender.info").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersWarnEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.WARN);
        appender.doAppend(event);

        assertThat(registry.meter("org.apache.log4j.Appender.all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter("org.apache.log4j.Appender.warn").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersErrorEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.ERROR);
        appender.doAppend(event);

        assertThat(registry.meter("org.apache.log4j.Appender.all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter("org.apache.log4j.Appender.error").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersFatalEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.FATAL);
        appender.doAppend(event);

        assertThat(registry.meter("org.apache.log4j.Appender.all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter("org.apache.log4j.Appender.fatal").getCount())
                .isEqualTo(1);
    }

    @Test
    public void usesSharedRegistries() throws Exception {
        SharedMetricRegistries.add("reg", registry);
        final InstrumentedAppender shared = new InstrumentedAppender("reg");
        shared.activateOptions();
        shared.doAppend(event);

        assertThat(registry.meter("org.apache.log4j.Appender.info").getCount())
                .isEqualTo(1);
    }
}
