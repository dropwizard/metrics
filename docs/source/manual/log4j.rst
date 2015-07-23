.. _manual-log4j:

####################
Instrumenting Log4j2
####################

The ``metrics-log4j2`` modules provide ``InstrumentedAppender``, a Log4j2 ``Appender`` implementation
which records the rate of logged events by their logging level.


You can add it to the root logger programmatically.

.. code-block:: java

    Filter filter = null;        // That's fine if we don't use filters; https://logging.apache.org/log4j/2.x/manual/filters.html
    PatternLayout layout = null; // The layout isn't used in InstrumentedAppender

    InstrumentedAppender appender = new InstrumentedAppender(metrics, filter, layout, false);
    appender.start();

    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    Configuration config = context.getConfiguration();
    config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).addAppender(appender, level, filter);
    context.updateLoggers(config);
