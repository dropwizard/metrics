package io.dropwizard.metrics5.health;

import io.dropwizard.metrics5.Clock;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HealthCheckTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

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
    void canHaveHealthyResults() {
        final HealthCheck.Result result = HealthCheck.Result.healthy();

        assertThat(result.isHealthy())
                .isTrue();

        assertThat(result.getMessage())
                .isNull();

        assertThat(result.getError())
                .isNull();
    }

    @Test
    void canHaveHealthyResultsWithMessages() {
        final HealthCheck.Result result = HealthCheck.Result.healthy("woo");

        assertThat(result.isHealthy())
                .isTrue();

        assertThat(result.getMessage())
                .isEqualTo("woo");

        assertThat(result.getError())
                .isNull();
    }

    @Test
    void canHaveHealthyResultsWithFormattedMessages() {
        final HealthCheck.Result result = HealthCheck.Result.healthy("foo %s", "bar");

        assertThat(result.isHealthy())
                .isTrue();

        assertThat(result.getMessage())
                .isEqualTo("foo bar");

        assertThat(result.getError())
                .isNull();
    }

    @Test
    void canHaveUnhealthyResults() {
        final HealthCheck.Result result = HealthCheck.Result.unhealthy("bad");

        assertThat(result.isHealthy())
                .isFalse();

        assertThat(result.getMessage())
                .isEqualTo("bad");

        assertThat(result.getError())
                .isNull();
    }

    @Test
    void canHaveUnhealthyResultsWithFormattedMessages() {
        final HealthCheck.Result result = HealthCheck.Result.unhealthy("foo %s %d", "bar", 123);

        assertThat(result.isHealthy())
                .isFalse();

        assertThat(result.getMessage())
                .isEqualTo("foo bar 123");

        assertThat(result.getError())
                .isNull();
    }

    @Test
    void canHaveUnhealthyResultsWithExceptions() {
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
    void canHaveHealthyBuilderWithFormattedMessage() {
        final HealthCheck.Result result = HealthCheck.Result.builder()
                .healthy()
                .withMessage("There are %d %s in the %s", 42, "foos", "bar")
                .build();

        assertThat(result.isHealthy())
                .isTrue();

        assertThat(result.getMessage())
                .isEqualTo("There are 42 foos in the bar");
    }

    @Test
    void canHaveHealthyBuilderWithDetail() {
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
    void canHaveUnHealthyBuilderWithDetail() {
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
    void canHaveUnHealthyBuilderWithDetailAndError() {
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
    void returnsResultsWhenExecuted() {
        final HealthCheck.Result result = mock(HealthCheck.Result.class);
        when(underlying.execute()).thenReturn(result);

        assertThat(healthCheck.execute())
                .isEqualTo(result);

        verify(result).setDuration(anyLong());
    }

    @Test
    void wrapsExceptionsWhenExecuted() {
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
        assertThat(actual.getDuration())
                .isGreaterThanOrEqualTo(0);
    }

    @Test
    void canHaveUserSuppliedClockForTimestamp() {
        ZonedDateTime dateTime = ZonedDateTime.now().minusMinutes(10);
        Clock clock = clockWithFixedTime(dateTime);

        HealthCheck.Result result = HealthCheck.Result.builder()
                .healthy()
                .usingClock(clock)
                .build();

        assertThat(result.isHealthy()).isTrue();

        assertThat(result.getTime()).isEqualTo(clock.getTime());

        assertThat(result.getTimestamp())
                .isEqualTo(DATE_TIME_FORMATTER.format(dateTime));
    }

    @Test
    void toStringWorksEvenForNullAttributes() {
        ZonedDateTime dateTime = ZonedDateTime.now().minusMinutes(25);
        Clock clock = clockWithFixedTime(dateTime);

        final HealthCheck.Result resultWithNullDetailValue = HealthCheck.Result.builder()
                .unhealthy()
                .withDetail("aNullDetail", null)
                .usingClock(clock)
                .build();
        assertThat(resultWithNullDetailValue.toString())
                .contains(
                        "Result{isHealthy=false, duration=0, timestamp=" + DATE_TIME_FORMATTER.format(dateTime),
                        ", aNullDetail=null}");
    }

    private static Clock clockWithFixedTime(ZonedDateTime dateTime) {
        return new Clock() {
            @Override
            public long getTick() {
                return 0;
            }

            @Override
            public long getTime() {
                return dateTime.toInstant().toEpochMilli();
            }
        };
    }
}
