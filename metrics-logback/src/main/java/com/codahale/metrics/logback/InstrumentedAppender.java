package com.codahale.metrics.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * A Logback {@link Appender} which has six meters, one for each logging level and one for the total
 * number of statements being logged. The meter names are the logging level names appended to the
 * name of the appender.
 */
public class InstrumentedAppender extends AppenderBase<ILoggingEvent> {
    private final MetricRegistry registry;

    private Meter all;
    private Meter trace;
    private Meter debug;
    private Meter info;
    private Meter warn;
    private Meter error;

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
        setName(Appender.class.getName());
    }

    @Override
    public void start() {
        this.all = registry.meter(name(getName(), "all"));
        this.trace = registry.meter(name(getName(), "trace"));
        this.debug = registry.meter(name(getName(), "debug"));
        this.info = registry.meter(name(getName(), "info"));
        this.warn = registry.meter(name(getName(), "warn"));
        this.error = registry.meter(name(getName(), "error"));
        super.start();
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
            default:
                break;
        }
    }
}
