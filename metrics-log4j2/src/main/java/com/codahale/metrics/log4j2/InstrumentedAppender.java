package com.codahale.metrics.log4j2;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * A Log4J 2.x {@link Appender} which has seven meters, one for each logging level and one for the total
 * number of statements being logged. The meter names are the logging level names appended to the
 * name of the appender.
 */
@Plugin(name = "MetricsAppender", category = "Core", elementType = "appender")
public class InstrumentedAppender extends AbstractAppender {

    private transient final MetricRegistry registry;

    private transient Meter all;
    private transient Meter trace;
    private transient Meter debug;
    private transient Meter info;
    private transient Meter warn;
    private transient Meter error;
    private transient Meter fatal;

    /**
     * Create a new instrumented appender using the given registry name.
     *
     * @param registryName     the name of the registry in {@link SharedMetricRegistries}
     * @param filter           The Filter to associate with the Appender.
     * @param layout           The layout to use to format the event.
     * @param ignoreExceptions If true, exceptions will be logged and suppressed. If false errors will be
     *                         logged and then passed to the application.
     */
    public InstrumentedAppender(String registryName, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        this(SharedMetricRegistries.getOrCreate(registryName), filter, layout, ignoreExceptions);
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
        this(registry, null, null, true);
    }

    /**
     * Create a new instrumented appender using the given registry.
     *
     * @param registry         the metric registry
     * @param filter           The Filter to associate with the Appender.
     * @param layout           The layout to use to format the event.
     * @param ignoreExceptions If true, exceptions will be logged and suppressed. If false errors will be
     *                         logged and then passed to the application.
     */
    public InstrumentedAppender(MetricRegistry registry, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name(Appender.class), filter, layout, ignoreExceptions);
        this.registry = registry;
    }

    /**
     * Create a new instrumented appender using the given appender name and registry.
     *
     * @param appenderName The name of the appender.
     * @param registry     the metric registry
     */
    public InstrumentedAppender(String appenderName, MetricRegistry registry) {
        super(appenderName, null, null, true);
        this.registry = registry;
    }

    @PluginFactory
    public static InstrumentedAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute(value = "registryName", defaultString = "log4j2Metrics") String registry) {
        return new InstrumentedAppender(name, SharedMetricRegistries.getOrCreate(registry));
    }

    @Override
    public void start() {
        this.all = registry.meter(name(getName(), "all"));
        this.trace = registry.meter(name(getName(), "trace"));
        this.debug = registry.meter(name(getName(), "debug"));
        this.info = registry.meter(name(getName(), "info"));
        this.warn = registry.meter(name(getName(), "warn"));
        this.error = registry.meter(name(getName(), "error"));
        this.fatal = registry.meter(name(getName(), "fatal"));
        super.start();
    }

    @Override
    public void append(LogEvent event) {
        all.mark();
        switch (event.getLevel().getStandardLevel()) {
            case TRACE:
                trace.mark();
                break;
            case DEBUG:
                debug.mark();
                break;
            case INFO:
                info.mark();
                break;
            case WARN:
                warn.mark();
                break;
            case ERROR:
                error.mark();
                break;
            case FATAL:
                fatal.mark();
                break;
            default:
                break;
        }
    }
}
