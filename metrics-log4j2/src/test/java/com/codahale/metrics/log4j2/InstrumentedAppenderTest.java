package com.codahale.metrics.log4j2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstrumentedAppenderTest {

    public static final String METRIC_NAME_PREFIX = "org.apache.logging.log4j.core.Appender";

    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedAppender appender = new InstrumentedAppender(registry);
    private final LogEvent event = mock(LogEvent.class);

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

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".trace").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersDebugEvents() {
        when(event.getLevel()).thenReturn(Level.DEBUG);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".debug").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersInfoEvents() {
        when(event.getLevel()).thenReturn(Level.INFO);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".info").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersWarnEvents() {
        when(event.getLevel()).thenReturn(Level.WARN);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".warn").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersErrorEvents() {
        when(event.getLevel()).thenReturn(Level.ERROR);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".error").getCount())
                .isEqualTo(1);
    }

    @Test
    public void metersFatalEvents() {
        when(event.getLevel()).thenReturn(Level.FATAL);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".fatal").getCount())
                .isEqualTo(1);
    }

    @Test
    public void usesSharedRegistries() {

        String registryName = "registry";

        SharedMetricRegistries.add(registryName, registry);

        final InstrumentedAppender shared = new InstrumentedAppender(registryName);
        shared.start();

        when(event.getLevel()).thenReturn(Level.INFO);

        shared.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".info").getCount())
                .isEqualTo(1);
    }
}
