package com.yammer.metrics.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.yammer.metrics.Meter;
import com.yammer.metrics.MetricRegistry;

import static com.yammer.metrics.MetricRegistry.name;

/**
 * A Logback {@link AppenderBase} which has six meters, one for each logging level and one for the
 * total number of statements being logged.
 */
public class InstrumentedAppender extends AppenderBase<ILoggingEvent> {
    private final Meter all;
    private final Meter trace;
    private final Meter debug;
    private final Meter info;
    private final Meter warn;
    private final Meter error;

    public InstrumentedAppender(MetricRegistry registry) {
        this(registry, null);
    }

    public InstrumentedAppender(MetricRegistry registry, String name) {
        this.all = registry.meter(name(Appender.class, name, "all"));
        this.trace = registry.meter(name(Appender.class, name, "trace"));
        this.debug = registry.meter(name(Appender.class, name, "debug"));
        this.info = registry.meter(name(Appender.class, name, "info"));
        this.warn = registry.meter(name(Appender.class, name, "warn"));
        this.error = registry.meter(name(Appender.class, name, "error"));
    }

    @Override
    protected void append(ILoggingEvent event) {
        all.mark();
        switch (event.getLevel().toInt()) {
            case Level.TRACE_INT:
                trace.mark();
                break;
            case Level.DEBUG_INT:
                debug.mark();
                break;
            case Level.INFO_INT:
                info.mark();
                break;
            case Level.WARN_INT:
                warn.mark();
                break;
            case Level.ERROR_INT:
                error.mark();
                break;
        }
    }
}
