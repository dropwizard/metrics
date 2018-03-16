package io.dropwizard.metrics5.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class HealthCheckTest {
    private static class ExampleHealthCheck implements HealthCheck {
        private final HealthCheck underlying;

        private ExampleHealthCheck(HealthCheck underlying) {
            this.underlying = underlying;
        }

        @Override
        public Result check() {
            return underlying.execute();
        }
    }

    private final HealthCheck underlying = mock(HealthCheck.class);
    private final HealthCheck healthCheck = new ExampleHealthCheck(underlying);

    @Test
    public void canHaveHealthyResults() {
        final HealthCheck.Result result = HealthCheck.Result.healthy();

        assertThat(result.isHealthy())
                .isTrue();

        assertThat(result.getMessage())
                .isNull();

        assertThat(result.getError())
                .isNull();
    }

    @Test
    public void canHaveHealthyResultsWithMessages() {
        final HealthCheck.Result result = HealthCheck.Result.healthy("woo");

        assertThat(result.isHealthy())
                .isTrue();

        assertThat(result.getMessage())
                .isEqualTo("woo");

        assertThat(result.getError())
                .isNull();
    }

    @Test
    public void canHaveHealthyResultsWithFormattedMessages() {
        final HealthCheck.Result result = HealthCheck.Result.healthy("foo %s", "bar");

        assertThat(result.isHealthy())
                .isTrue();

        assertThat(result.getMessage())
                .isEqualTo("foo bar");

        assertThat(result.getError())
                .isNull();
    }

    @Test
    public void canHaveUnhealthyResults() {
        final HealthCheck.Result result = HealthCheck.Result.unhealthy("bad");

        assertThat(result.isHealthy())
                .isFalse();

        assertThat(result.getMessage())
                .isEqualTo("bad");

        assertThat(result.getError())
                .isNull();
    }

    @Test
    public void canHaveUnhealthyResultsWithFormattedMessages() {
        final HealthCheck.Result result = HealthCheck.Result.unhealthy("foo %s %d", "bar", 123);

        assertThat(result.isHealthy())
                .isFalse();

        assertThat(result.getMessage())
                .isEqualTo("foo bar 123");

        assertThat(result.getError())
                .isNull();
    }

    @Test
    public void canHaveUnhealthyResultsWithExceptions() {
        final RuntimeException e = mock(RuntimeException.class);
        when(e.getMessage()).thenReturn("oh noes");

        final HealthCheck.Result result = HealthCheck.Result.unhealthy(e);

        assertThat(result.isHealthy())
                .isFalse();

        assertThat(result.getMessage())
                .isEqualTo("oh noes");

        assertThat(result.getError())
                .isEqualTo(e);
    }

    @Test
    public void canHaveHealthyBuilderWithDetail() {
        final HealthCheck.Result result = HealthCheck.Result.builder()
                .healthy()
                .withDetail("detail", "value")
                .build();

        assertThat(result.isHealthy())
                .isTrue();

        assertThat(result.getMessage())
                .isNull();

        assertThat(result.getError())
                .isNull();

        assertThat(result.getDetails())
                .containsEntry("detail", "value");
    }

    @Test
    public void canHaveUnHealthyBuilderWithDetail() {
        final HealthCheck.Result result = HealthCheck.Result.builder()
                .unhealthy()
                .withDetail("detail", "value")
                .build();

        assertThat(result.isHealthy())
                .isFalse();

        assertThat(result.getMessage())
                .isNull();

        assertThat(result.getError())
                .isNull();

        assertThat(result.getDetails())
                .containsEntry("detail", "value");
    }

    @Test
    public void canHaveUnHealthyBuilderWithDetailAndError() {
        final RuntimeException e = mock(RuntimeException.class);
        when(e.getMessage()).thenReturn("oh noes");

        final HealthCheck.Result result = HealthCheck.Result
                .builder()
                .unhealthy(e)
                .withDetail("detail", "value")
                .build();

        assertThat(result.isHealthy())
                .isFalse();

        assertThat(result.getMessage())
                .isEqualTo("oh noes");

        assertThat(result.getError())
                .isEqualTo(e);

        assertThat(result.getDetails())
                .containsEntry("detail", "value");
    }

    @Test
    public void returnsResultsWhenExecuted() {
        final HealthCheck.Result result = mock(HealthCheck.Result.class);
        when(underlying.execute()).thenReturn(result);

        assertThat(healthCheck.execute())
                .isEqualTo(result);
    }

    @Test
    public void wrapsExceptionsWhenExecuted() {
        final RuntimeException e = mock(RuntimeException.class);
        when(e.getMessage()).thenReturn("oh noes");

        when(underlying.execute()).thenThrow(e);
        HealthCheck.Result actual = healthCheck.execute();

        assertThat(actual.isHealthy())
                .isFalse();
        assertThat(actual.getMessage())
                .isEqualTo("oh noes");
        assertThat(actual.getError())
                .isEqualTo(e);
        assertThat(actual.getDetails())
                .isNull();
    }

    @Test
    public void toStringWorksEvenForNullAttributes() {
        final HealthCheck.Result resultWithNullDetailValue = HealthCheck.Result.builder()
                .unhealthy()
                .withDetail("aNullDetail", null)
                .build();
        assertThat(resultWithNullDetailValue.toString())
                .contains(
                        "Result{isHealthy=false, timestamp=", // Skip the timestamp part of the String.
                        ", aNullDetail=null}");
    }
}
