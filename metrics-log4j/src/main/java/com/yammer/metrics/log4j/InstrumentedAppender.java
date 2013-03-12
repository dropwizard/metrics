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
    private final MetricsRegistry metricsRegistry;

    private String scope;

    private Meter all;
    private Meter trace;
    private Meter debug;
    private Meter info;
    private Meter warn;
    private Meter error;
    private Meter fatal;

    public InstrumentedAppender() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedAppender(MetricsRegistry registry) {
        super();
        this.metricsRegistry = registry;
    }

    @Override
    public void activateOptions() {
        this.all = newMeter("all");
        this.trace = newMeter("trace");
        this.debug = newMeter("debug");
        this.info = newMeter("info");
        this.warn = newMeter("warn");
        this.error = newMeter("error");
        this.fatal = newMeter("fatal");
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

    /**
     * Sets the scope for the metrics name, default: {@code null}.
     *
     * @param scope The scope.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Creates a new meter and registers it under the following metrics name.
     *
     * <ol>
     *     <li><b>Group:</b> {@code "org.apache.log4j"}</li>
     *     <li><b>Type:</b> {@code "Appender"}</li>
     *     <li><b>Scope:</b> The scope set via {@link #setScope(String)}.</li>
     *     <li><b>Name:</b> The specified name.</li>
     * </ol>
     *
     * @param name The name.
     * @return The meter.
     */
    private Meter newMeter(String name) {
        return (this.scope != null)
                ? this.metricsRegistry.newMeter(Appender.class, name, this.scope, "statements", TimeUnit.SECONDS)
                : this.metricsRegistry.newMeter(Appender.class, name, "statements", TimeUnit.SECONDS);
    }
}
