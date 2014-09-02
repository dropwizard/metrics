.. _manual-log4j:

###################
Instrumenting Log4j
###################

The ``metrics-log4j`` and ``metrics-log4j2`` modules provide ``InstrumentedAppender``, a Log4j ``Appender`` implementation
(for log4j 1.x and log4j 2.x correspondingly) which records the rate of logged events by their logging level.


You can add it to the root logger programmatically.

For log4j 1.x:

.. code-block:: java
    InstrumentedAppender appender = new InstrumentedAppender(registry);
    appender.activateOptions();
    LogManager.getRootLogger().addAppender(appender);


For log4j 2.x:

.. code-block:: java
     InstrumentedAppender appender = new InstrumentedAppender(registry, filter, layout, true);
     appender.start();
     LogManager.getRootLogger().addAppender(appender);