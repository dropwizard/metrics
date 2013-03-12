package com.yammer.metrics.log4j;

import com.yammer.metrics.Meter;
import com.yammer.metrics.MetricRegistry;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import static com.yammer.metrics.MetricRegistry.name;

/**
 * A Log4J {@link Appender} delegate which has seven meters, one for each logging level and one for
 * the total number of statements being logged.
 */
public class InstrumentedAppender extends AppenderSkeleton {
    private final Meter all;
    private final Meter trace;
    private final Meter debug;
    private final Meter info;
    private final Meter warn;
    private final Meter error;
    private final Meter fatal;

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
        this.fatal = registry.meter(name(Appender.class, name, "fatal"));
    }

    @Override
    protected void append(LoggingEvent event) {
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
            case Level.FATAL_INT:
                fatal.mark();
                break;
        }
    }

    @Override
    public void close() {
        // nothing doing
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
