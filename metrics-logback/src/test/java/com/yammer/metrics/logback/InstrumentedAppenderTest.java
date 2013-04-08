package com.yammer.metrics.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.yammer.metrics.Meter;
import com.yammer.metrics.MetricRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.yammer.metrics.MetricRegistry.name;
import static org.mockito.Mockito.*;

public class InstrumentedAppenderTest {
    private Meter all, trace, debug, info, warn, error;
    private ILoggingEvent event;
    private InstrumentedAppender instrumented;

    @Before
    public void setUp() throws Exception {
        this.all = mock(Meter.class);
        this.trace = mock(Meter.class);
        this.debug = mock(Meter.class);
        this.info = mock(Meter.class);
        this.warn = mock(Meter.class);
        this.error = mock(Meter.class);

        this.event = mock(ILoggingEvent.class);
        when(event.getLevel()).thenReturn(Level.INFO);

        final MetricRegistry registry = mock(MetricRegistry.class);
        when(registry.meter(name(Appender.class, "all"))).thenReturn(all);
        when(registry.meter(name(Appender.class, "trace"))).thenReturn(trace);
        when(registry.meter(name(Appender.class, "debug"))).thenReturn(debug);
        when(registry.meter(name(Appender.class, "info"))).thenReturn(info);
        when(registry.meter(name(Appender.class, "warn"))).thenReturn(warn);
        when(registry.meter(name(Appender.class, "error"))).thenReturn(error);

        this.instrumented = new InstrumentedAppender(registry);
        instrumented.start();
    }

    @After
    public void tearDown() throws Exception {
        instrumented.stop();
    }

    @Test
    public void metersTraceEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.TRACE);
        instrumented.doAppend(event);

        verify(trace).mark();
        verify(all).mark();
    }

    @Test
    public void metersDebugEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.DEBUG);
        instrumented.doAppend(event);

        verify(debug).mark();
        verify(all).mark();
    }

    @Test
    public void metersInfoEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.INFO);
        instrumented.doAppend(event);

        verify(info).mark();
        verify(all).mark();
    }

    @Test
    public void metersWarnEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.WARN);
        instrumented.doAppend(event);

        verify(warn).mark();
        verify(all).mark();
    }

    @Test
    public void metersErrorEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.ERROR);
        instrumented.doAppend(event);

        verify(error).mark();
        verify(all).mark();
    }
}
