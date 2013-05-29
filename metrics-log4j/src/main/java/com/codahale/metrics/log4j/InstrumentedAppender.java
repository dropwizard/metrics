package com.codahale.metrics.log4j;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * A Log4J {@link Appender} which has seven meters, one for each logging level and one for the total
 * number of statements being logged. The meter names are the logging level names appended to the
 * name of the appender.
 */
public class InstrumentedAppender extends AppenderSkeleton {
    private final MetricRegistry registry;

    private Meter all;
    private Meter trace;
    private Meter debug;
    private Meter info;
    private Meter warn;
    private Meter error;
    private Meter fatal;

    /**
     * Create a new instrumented appender using the given registry name.
     *
     * @param registryName the name of the registry in {@link SharedMetricRegistries}
     */
    public InstrumentedAppender(String registryName) {
        this(SharedMetricRegistries.getOrCreate(registryName));
    }

    /**
     * Create a new instrumented appender using the given registry.
     *
     * @param registry the metric registry
     */
    public InstrumentedAppender(MetricRegistry registry) {
        this.registry = registry;
        setName(name(Appender.class));
    }

    @Override
    public void activateOptions() {
        this.all = registry.meter(name(getName(), "all"));
        this.trace = registry.meter(name(getName(), "trace"));
        this.debug = registry.meter(name(getName(), "debug"));
        this.info = registry.meter(name(getName(), "info"));
        this.warn = registry.meter(name(getName(), "warn"));
        this.error = registry.meter(name(getName(), "error"));
        this.fatal = registry.meter(name(getName(), "fatal"));
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
            default:
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
