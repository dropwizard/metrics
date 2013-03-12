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

public class InstrumentedAppenderWithScopeTest {
    private static final String SAMPLE_SCOPE = "sampleScope";

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
        when(registry.newMeter(Appender.class, "all", SAMPLE_SCOPE, "statements", TimeUnit.SECONDS)).thenReturn(all);
        when(registry.newMeter(Appender.class, "trace", SAMPLE_SCOPE, "statements", TimeUnit.SECONDS)).thenReturn(trace);
        when(registry.newMeter(Appender.class, "debug", SAMPLE_SCOPE, "statements", TimeUnit.SECONDS)).thenReturn(debug);
        when(registry.newMeter(Appender.class, "info", SAMPLE_SCOPE, "statements", TimeUnit.SECONDS)).thenReturn(info);
        when(registry.newMeter(Appender.class, "warn", SAMPLE_SCOPE, "statements", TimeUnit.SECONDS)).thenReturn(warn);
        when(registry.newMeter(Appender.class, "error", SAMPLE_SCOPE, "statements", TimeUnit.SECONDS)).thenReturn(error);
        when(registry.newMeter(Appender.class, "fatal", SAMPLE_SCOPE, "statements", TimeUnit.SECONDS)).thenReturn(fatal);

        this.instrumented = new InstrumentedAppender(registry);
        this.instrumented.setScope(SAMPLE_SCOPE);
        this.instrumented.activateOptions(); // lifecycle method usually called by the log4j framework
    }

    @Test
    public void metersTraceEventsWithScope() throws Exception {
        when(event.getLevel()).thenReturn(Level.TRACE);
        instrumented.doAppend(event);

        verify(trace).mark();
        verify(all).mark();
    }

    @Test
    public void metersDebugEventsWithScope() throws Exception {
        when(event.getLevel()).thenReturn(Level.DEBUG);
        instrumented.doAppend(event);

        verify(debug).mark();
        verify(all).mark();
    }

    @Test
    public void metersInfoEventsWithScope() throws Exception {
        when(event.getLevel()).thenReturn(Level.INFO);
        instrumented.doAppend(event);

        verify(info).mark();
        verify(all).mark();
    }

    @Test
    public void metersWarnEventsWithScope() throws Exception {
        when(event.getLevel()).thenReturn(Level.WARN);
        instrumented.doAppend(event);

        verify(warn).mark();
        verify(all).mark();
    }

    @Test
    public void metersErrorEventsWithScope() throws Exception {
        when(event.getLevel()).thenReturn(Level.ERROR);
        instrumented.doAppend(event);

        verify(error).mark();
        verify(all).mark();
    }

    @Test
    public void metersFatalEventsWithScope() throws Exception {
        when(event.getLevel()).thenReturn(Level.FATAL);
        instrumented.doAppend(event);

        verify(fatal).mark();
        verify(all).mark();
    }
}
