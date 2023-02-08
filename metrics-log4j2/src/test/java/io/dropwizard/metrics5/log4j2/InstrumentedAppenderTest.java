package io.dropwizard.metrics5.log4j2;

import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.SharedMetricRegistries;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstrumentedAppenderTest {

    public static final String METRIC_NAME_PREFIX = "org.apache.logging.log4j.core.Appender";

    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedAppender appender = new InstrumentedAppender(registry);
    private final LogEvent event = mock(LogEvent.class);

    @BeforeEach
    void setUp() {
        appender.start();
    }

    @AfterEach
    void tearDown() {
        SharedMetricRegistries.clear();
    }

    @Test
    void metersTraceEvents() {
        when(event.getLevel()).thenReturn(Level.TRACE);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".trace").getCount())
                .isEqualTo(1);
    }

    @Test
    void metersDebugEvents() {
        when(event.getLevel()).thenReturn(Level.DEBUG);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".debug").getCount())
                .isEqualTo(1);
    }

    @Test
    void metersInfoEvents() {
        when(event.getLevel()).thenReturn(Level.INFO);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".info").getCount())
                .isEqualTo(1);
    }

    @Test
    void metersWarnEvents() {
        when(event.getLevel()).thenReturn(Level.WARN);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".warn").getCount())
                .isEqualTo(1);
    }

    @Test
    void metersErrorEvents() {
        when(event.getLevel()).thenReturn(Level.ERROR);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".error").getCount())
                .isEqualTo(1);
    }

    @Test
    void metersFatalEvents() {
        when(event.getLevel()).thenReturn(Level.FATAL);

        appender.append(event);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(1);

        assertThat(registry.meter(METRIC_NAME_PREFIX + ".fatal").getCount())
                .isEqualTo(1);
    }

    @Test
    void usesSharedRegistries() {

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
