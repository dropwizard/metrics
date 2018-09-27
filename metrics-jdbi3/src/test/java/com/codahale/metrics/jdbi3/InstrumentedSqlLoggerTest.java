package com.codahale.metrics.jdbi3;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.jdbi3.strategies.StatementNameStrategy;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Test;

import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class InstrumentedSqlLoggerTest {
    @Test
    public void logsExecutionTime() {
        final MetricRegistry mockRegistry = mock(MetricRegistry.class);
        final StatementNameStrategy mockNameStrategy = mock(StatementNameStrategy.class);
        final InstrumentedSqlLogger logger = new InstrumentedSqlLogger(mockRegistry, mockNameStrategy);

        final StatementContext mockContext = mock(StatementContext.class);
        final Timer mockTimer = mock(Timer.class);

        final MetricName statementName = MetricName.build("my-fake-name");
        final long fakeElapsed = 1234L;

        when(mockNameStrategy.getStatementName(mockContext)).thenReturn(statementName);
        when(mockRegistry.timer(statementName)).thenReturn(mockTimer);

        when(mockContext.getElapsedTime(ChronoUnit.NANOS)).thenReturn(fakeElapsed);

        logger.logAfterExecution(mockContext);

        verify(mockTimer).update(fakeElapsed, TimeUnit.NANOSECONDS);
    }

    @Test
    public void logsExceptionTime() {
        final MetricRegistry mockRegistry = mock(MetricRegistry.class);
        final StatementNameStrategy mockNameStrategy = mock(StatementNameStrategy.class);
        final InstrumentedSqlLogger logger = new InstrumentedSqlLogger(mockRegistry, mockNameStrategy);

        final StatementContext mockContext = mock(StatementContext.class);
        final Timer mockTimer = mock(Timer.class);

        final MetricName statementName = MetricName.build("my-fake-name");
        final long fakeElapsed = 1234L;

        when(mockNameStrategy.getStatementName(mockContext)).thenReturn(statementName);
        when(mockRegistry.timer(statementName)).thenReturn(mockTimer);

        when(mockContext.getElapsedTime(ChronoUnit.NANOS)).thenReturn(fakeElapsed);

        logger.logException(mockContext, new SQLException());

        verify(mockTimer).update(fakeElapsed, TimeUnit.NANOSECONDS);
    }
}
