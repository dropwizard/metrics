package com.yammer.metrics.log4j.tests;

import com.yammer.metrics.Meter;
import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.log4j.InstrumentedAppender;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;

import static com.yammer.metrics.MetricRegistry.name;
import static org.mockito.Mockito.*;

public class InstrumentedAppenderTest {
    private Meter all, trace, debug, info, warn, error, fatal;
    private LoggingEvent event;
    private InstrumentedAppender instrumented;

    @Before
    public void setUp() throws Exception {
        this.all = mock(Meter.class);
        this.trace = mock(Meter.class);
        this.debug = mock(Meter.class);
        this.info = mock(Meter.class);
        this.warn = mock(Meter.class);
        this.error = mock(Meter.class);
        this.fatal = mock(Meter.class);
        
        this.event = mock(LoggingEvent.class);
        when(event.getLevel()).thenReturn(Level.INFO);

        final MetricRegistry registry = mock(MetricRegistry.class);
        when(registry.meter(name(Appender.class, "all"))).thenReturn(all);
        when(registry.meter(name(Appender.class, "trace"))).thenReturn(trace);
        when(registry.meter(name(Appender.class, "debug"))).thenReturn(debug);
        when(registry.meter(name(Appender.class, "info"))).thenReturn(info);
        when(registry.meter(name(Appender.class, "warn"))).thenReturn(warn);
        when(registry.meter(name(Appender.class, "error"))).thenReturn(error);
        when(registry.meter(name(Appender.class, "fatal"))).thenReturn(fatal);

        this.instrumented = new InstrumentedAppender(registry);
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

    @Test
    public void metersFatalEvents() throws Exception {
        when(event.getLevel()).thenReturn(Level.FATAL);
        instrumented.doAppend(event);

        verify(fatal).mark();
        verify(all).mark();
    }
}
