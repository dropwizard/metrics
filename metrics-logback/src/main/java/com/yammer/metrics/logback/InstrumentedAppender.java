package com.yammer.metrics.logback;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.concurrent.TimeUnit;

/**
 * A Logback {@link AppenderBase} which has six meters, one for each logging level and one for the
 * total number of statements being logged.
 */
public class InstrumentedAppender extends AppenderBase<ILoggingEvent> {
    static final Meter ALL_METER = Metrics.newMeter(InstrumentedAppender.class, "all", "statements", TimeUnit.SECONDS);
    static final Meter TRACE_METER = Metrics.newMeter(InstrumentedAppender.class, "trace", "statements", TimeUnit.SECONDS);
    static final Meter DEBUG_METER = Metrics.newMeter(InstrumentedAppender.class, "debug", "statements", TimeUnit.SECONDS);
    static final Meter INFO_METER = Metrics.newMeter(InstrumentedAppender.class, "info", "statements", TimeUnit.SECONDS);
    static final Meter WARN_METER = Metrics.newMeter(InstrumentedAppender.class, "warn", "statements", TimeUnit.SECONDS);
    static final Meter ERROR_METER = Metrics.newMeter(InstrumentedAppender.class, "error", "statements", TimeUnit.SECONDS);

    @Override
    protected void append(ILoggingEvent event) {
        ALL_METER.mark();
        if (event.getLevel().toInt() == Level.TRACE_INT) {
            TRACE_METER.mark();
        } else if (event.getLevel().toInt() == Level.DEBUG_INT) {
            DEBUG_METER.mark();
        } else if (event.getLevel().toInt() == Level.INFO_INT) {
            INFO_METER.mark();
        } else if (event.getLevel().toInt() == Level.WARN_INT) {
            WARN_METER.mark();
        } else if (event.getLevel().toInt() == Level.ERROR_INT) {
            ERROR_METER.mark();
        }
    }
}
