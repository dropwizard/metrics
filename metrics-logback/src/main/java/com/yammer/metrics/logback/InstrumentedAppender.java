package com.yammer.metrics.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;

import java.util.concurrent.TimeUnit;

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

    public InstrumentedAppender() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedAppender(MetricsRegistry registry) {
        this.all = registry.newMeter(Appender.class, "all", "statements", TimeUnit.SECONDS);
        this.trace = registry.newMeter(Appender.class, "trace", "statements", TimeUnit.SECONDS);
        this.debug = registry.newMeter(Appender.class, "debug", "statements", TimeUnit.SECONDS);
        this.info = registry.newMeter(Appender.class, "info", "statements", TimeUnit.SECONDS);
        this.warn = registry.newMeter(Appender.class, "warn", "statements", TimeUnit.SECONDS);
        this.error = registry.newMeter(Appender.class, "error", "statements", TimeUnit.SECONDS);
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
