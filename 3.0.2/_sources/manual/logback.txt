.. _manual-logback:

#####################
Instrumenting Logback
#####################

The ``metrics-logback`` module provides ``InstrumentedAppender``, a Logback ``Appender``
implementation which records the rate of logged events by their logging level.

You add it to the root logger programmatically:

.. code-block:: java

    final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
    final Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

    final InstrumentedAppender metrics = new InstrumentedAppender(registry);
    metrics.setContext(root.getLoggerContext());
    metrics.start();
    root.addAppender(metrics);
