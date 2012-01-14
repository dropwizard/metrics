package com.yammer.metrics.log4j.tests;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.log4j.InstrumentedAppender;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

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

        final MetricsRegistry registry = mock(MetricsRegistry.class);
        when(registry.newMeter(Appender.class, "all", "statements", TimeUnit.SECONDS)).thenReturn(all);
        when(registry.newMeter(Appender.class, "trace", "statements", TimeUnit.SECONDS)).thenReturn(trace);
        when(registry.newMeter(Appender.class, "debug", "statements", TimeUnit.SECONDS)).thenReturn(debug);
        when(registry.newMeter(Appender.class, "info", "statements", TimeUnit.SECONDS)).thenReturn(info);
        when(registry.newMeter(Appender.class, "warn", "statements", TimeUnit.SECONDS)).thenReturn(warn);
        when(registry.newMeter(Appender.class, "error", "statements", TimeUnit.SECONDS)).thenReturn(error);
        when(registry.newMeter(Appender.class, "fatal", "statements", TimeUnit.SECONDS)).thenReturn(fatal);

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
