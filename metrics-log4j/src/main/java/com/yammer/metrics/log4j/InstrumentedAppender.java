package com.yammer.metrics.log4j;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.TimeUnit;

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

    public InstrumentedAppender() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedAppender(MetricsRegistry registry) {
        super();
        this.all = registry.newMeter(Appender.class, "all", "statements", TimeUnit.SECONDS);
        this.trace = registry.newMeter(Appender.class, "trace", "statements", TimeUnit.SECONDS);
        this.debug = registry.newMeter(Appender.class, "debug", "statements", TimeUnit.SECONDS);
        this.info = registry.newMeter(Appender.class, "info", "statements", TimeUnit.SECONDS);
        this.warn = registry.newMeter(Appender.class, "warn", "statements", TimeUnit.SECONDS);
        this.error = registry.newMeter(Appender.class, "error", "statements", TimeUnit.SECONDS);
        this.fatal = registry.newMeter(Appender.class, "fatal", "statements", TimeUnit.SECONDS);
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
