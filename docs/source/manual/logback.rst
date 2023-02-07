.. _manual-logback:

#####################
Instrumenting Logback
#####################

The ``metrics-logback13`` and ``metrics-logback14`` modules provide ``InstrumentedAppender``,
a Logback_ ``Appender`` implementation which records the rate of logged events by their logging level.

.. _Logback: https://logback.qos.ch/

You add it to the root logger programmatically:

.. code-block:: java

    final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
    final Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

    final InstrumentedAppender metrics = new InstrumentedAppender(registry);
    metrics.setContext(root.getLoggerContext());
    metrics.start();
    root.addAppender(metrics);
