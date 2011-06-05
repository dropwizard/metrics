package com.yammer.metrics.log4j;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MeterMetric;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.TimeUnit;

/**
 * A Log4J {@link Appender} delegate which has seven meters, one for each
 * logging level and one for the total number of statements being logged.
 */
public class InstrumentedAppender implements Appender {
    private static final MeterMetric ALL_METER = Metrics.newMeter(InstrumentedAppender.class, "all", "statements", TimeUnit.SECONDS);
    private static final MeterMetric TRACE_METER = Metrics.newMeter(InstrumentedAppender.class, "trace", "statements", TimeUnit.SECONDS);
    private static final MeterMetric DEBUG_METER = Metrics.newMeter(InstrumentedAppender.class, "debug", "statements", TimeUnit.SECONDS);
    private static final MeterMetric INFO_METER = Metrics.newMeter(InstrumentedAppender.class, "info", "statements", TimeUnit.SECONDS);
    private static final MeterMetric WARN_METER = Metrics.newMeter(InstrumentedAppender.class, "warn", "statements", TimeUnit.SECONDS);
    private static final MeterMetric ERROR_METER = Metrics.newMeter(InstrumentedAppender.class, "error", "statements", TimeUnit.SECONDS);
    private static final MeterMetric FATAL_METER = Metrics.newMeter(InstrumentedAppender.class, "fatal", "statements", TimeUnit.SECONDS);

    private final Appender underlying;

    public InstrumentedAppender(Appender underlying) {
        this.underlying = underlying;
    }

    @Override
    public void addFilter(Filter newFilter) {
        underlying.addFilter(newFilter);
    }

    @Override
    public void clearFilters() {
        underlying.clearFilters();
    }

    @Override
    public void close() {
        underlying.close();
    }

    @Override
    public void doAppend(LoggingEvent event) {
        ALL_METER.mark();
        if (event.getLevel() == Level.TRACE) {
            TRACE_METER.mark();
        } else if (event.getLevel() == Level.DEBUG) {
            DEBUG_METER.mark();
        } else if (event.getLevel() == Level.INFO) {
            INFO_METER.mark();
        } else if (event.getLevel() == Level.WARN) {
            WARN_METER.mark();
        } else if (event.getLevel() == Level.ERROR) {
            ERROR_METER.mark();
        } else if (event.getLevel() == Level.FATAL) {
            FATAL_METER.mark();
        }
        underlying.doAppend(event);
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return underlying.getErrorHandler();
    }

    @Override
    public Filter getFilter() {
        return underlying.getFilter();
    }

    @Override
    public Layout getLayout() {
        return underlying.getLayout();
    }

    @Override
    public String getName() {
        return underlying.getName();
    }

    @Override
    public boolean requiresLayout() {
        return underlying.requiresLayout();
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        underlying.setErrorHandler(errorHandler);
    }

    @Override
    public void setLayout(Layout layout) {
        underlying.setLayout(layout);
    }

    @Override
    public void setName(String name) {
        underlying.setName(name);
    }
}
