.. _manual-log4j:

###################
Instrumenting Log4j
###################

The ``metrics-log4j`` module provides ``InstrumentedAppender``, a Log4j ``Appender`` implementation
which records the rate of logged events by their logging level.

You can either add it to the root logger programmatically:

.. code-block:: java

    LogManager.getRootLogger().addAppender(new InstrumentedAppender());

Or you can add it via Log4j's XML configuration:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8" ?>
    <!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">

    <log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">
        <appender name="console" class="org.apache.log4j.ConsoleAppender">
            <param name="Target" value="System.out"/>
            <layout class="org.apache.log4j.PatternLayout">
                <param name="ConversionPattern" value="%-5p %c{1} - %m%n"/>
            </layout>
        </appender>

        <appender name="metrics" class="com.yammer.metrics.log4j.InstrumentedAppender"/>

        <root>
            <priority value="debug"/>
            <appender-ref ref="console"/>
            <appender-ref ref="metrics"/>
        </root>
    </log4j:configuration>
