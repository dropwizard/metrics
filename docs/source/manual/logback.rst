.. _manual-logback:

#####################
Instrumenting Logback
#####################

The ``metrics-logback`` module provides ``InstrumentedAppender``, a Logback ``Appender``
implementation which records the rate of logged events by their logging level.

You can either add it to the root logger programmatically:

.. code-block:: java

    final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
    final Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

    final InstrumentedAppender metrics = new InstrumentedAppender();
    metrics.setContext(root.getLoggerContext());
    metrics.start();
    root.addAppender(metrics);

Or you can add it via Logback's XML configuration:

.. code-block:: xml

    <configuration>
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%-4relative [%thread] %-5level %logger{35} - %msg %n</pattern>
            </encoder>
        </appender>

        <appender name="metrics" class="com.yammer.metrics.logback.InstrumentedAppender"/>

        <root level="DEBUG">
            <appender-ref ref="STDOUT"/>
            <appender-ref ref="metrics"/>
        </root>
    </configuration>
