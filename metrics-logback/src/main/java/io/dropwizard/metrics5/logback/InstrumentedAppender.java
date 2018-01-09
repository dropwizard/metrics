package io.dropwizard.metrics5.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.SharedMetricRegistries;

/**
 * A Logback {@link Appender} which has six meters, one for each logging level and one for the total
 * number of statements being logged. The meter names are the logging level names appended to the
 * name of the appender.
 */
public class InstrumentedAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private final MetricRegistry registry;
    public static final String DEFAULT_REGISTRY = "logback-metrics";
    public static final String REGISTRY_PROPERTY_NAME = "metrics.logback.registry";

    private Meter all;
    private Meter trace;
    private Meter debug;
    private Meter info;
    private Meter warn;
    private Meter error;


    /**
     * Create a new instrumented appender using the given registry name.
     */
    public InstrumentedAppender() {
        this(System.getProperty(REGISTRY_PROPERTY_NAME, DEFAULT_REGISTRY));
    }

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
        this.all = registry.meter(MetricRegistry.name(getName(), "all"));
        this.trace = registry.meter(MetricRegistry.name(getName(), "trace"));
        this.debug = registry.meter(MetricRegistry.name(getName(), "debug"));
        this.info = registry.meter(MetricRegistry.name(getName(), "info"));
        this.warn = registry.meter(MetricRegistry.name(getName(), "warn"));
        this.error = registry.meter(MetricRegistry.name(getName(), "error"));
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
