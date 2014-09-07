.. _manual-log4j:

###################
Instrumenting Log4j
###################

The ``metrics-log4j`` module provides ``InstrumentedAppender``, a Log4j ``Appender`` implementation
which records the rate of logged events by their logging level.

You can add it to the root logger programmatically:

.. code-block:: java

    LogManager.getRootLogger().addAppender(new InstrumentedAppender(registry));
