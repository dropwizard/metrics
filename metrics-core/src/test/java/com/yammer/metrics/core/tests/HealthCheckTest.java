package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.HealthCheck;
import org.junit.Test;

import static com.yammer.metrics.core.HealthCheck.Result;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HealthCheckTest {
    private static class ExampleHealthCheck extends HealthCheck {
        private final HealthCheck underlying;

        private ExampleHealthCheck(HealthCheck underlying) {
            super("example");
            this.underlying = underlying;
        }

        @Override
        protected Result check() throws Exception {
            return underlying.execute();
        }
    }

    private final HealthCheck underlying = mock(HealthCheck.class);
    private final HealthCheck healthCheck = new ExampleHealthCheck(underlying);

    @Test
    public void hasAName() throws Exception {
        assertThat(healthCheck.getName(),
                   is("example"));
    }

    @Test
    public void canHaveHealthyResults() throws Exception {
        final Result result = Result.healthy();
        
        assertThat(result.isHealthy(),
                   is(true));
        
        assertThat(result.getMessage(),
                   is(nullValue()));

        assertThat(result.getError(),
                   is(nullValue()));
    }

    @Test
    public void canHaveHealthyResultsWithMessages() throws Exception {
        final Result result = Result.healthy("woo");

        assertThat(result.isHealthy(),
                   is(true));

        assertThat(result.getMessage(),
                   is("woo"));

        assertThat(result.getError(),
                   is(nullValue()));
    }

    @Test
    public void canHaveHealthyResultsWithFormattedMessages() throws Exception {
        final Result result = Result.healthy("foo %s", "bar");

        assertThat(result.isHealthy(),
                    is(true));
        
        assertThat(result.getMessage(),
                    is("foo bar"));

        assertThat(result.getError(),
                    is(nullValue()));
    }

    @Test
    public void canHaveUnhealthyResults() throws Exception {
        final Result result = Result.unhealthy("bad");

        assertThat(result.isHealthy(),
                   is(false));

        assertThat(result.getMessage(),
                   is("bad"));

        assertThat(result.getError(),
                   is(nullValue()));
    }

    @Test
    public void canHaveUnhealthyResultsWithFormattedMessages() throws Exception {
        final Result result = Result.unhealthy("foo %s %d", "bar", 123);

        assertThat(result.isHealthy(),
                    is(false));
        
        assertThat(result.getMessage(),
                    is("foo bar 123"));

        assertThat(result.getError(),
                    is(nullValue()));
    }

    @Test
    public void canHaveUnhealthyResultsWithExceptions() throws Exception {
        final RuntimeException e = mock(RuntimeException.class);
        when(e.getMessage()).thenReturn("oh noes");

        final Result result = Result.unhealthy(e);

        assertThat(result.isHealthy(),
                   is(false));

        assertThat(result.getMessage(),
                   is("oh noes"));

        assertThat(result.getError(),
                   is((Throwable) e));
    }

    @Test
    public void returnsResultsWhenExecuted() throws Exception {
        final Result result = mock(Result.class);
        when(underlying.execute()).thenReturn(result);

        assertThat(healthCheck.execute(),
                   is(result));
    }

    @Test
    public void wrapsExceptionsWhenExecuted() throws Exception {
        final RuntimeException e = mock(RuntimeException.class);
        when(e.getMessage()).thenReturn("oh noes");
        
        when(underlying.execute()).thenThrow(e);

        assertThat(healthCheck.execute(),
                   is(Result.unhealthy(e)));
    }
}
