package io.dropwizard.metrics5.health;

import io.dropwizard.metrics5.Clock;
import io.dropwizard.metrics5.health.annotation.Async;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AsyncHealthCheckDecorator}.
 */
class AsyncHealthCheckDecoratorTest {

    private static final long CURRENT_TIME = 1551002401000L;

    private static final Clock FIXED_CLOCK = clockWithFixedTime(CURRENT_TIME);

    private static final HealthCheck.Result EXPECTED_EXPIRED_RESULT = HealthCheck.Result
            .builder()
            .usingClock(FIXED_CLOCK)
            .unhealthy()
            .withMessage("Result was healthy but it expired 1 milliseconds ago")
            .build();

    private final HealthCheck mockHealthCheck = mock(HealthCheck.class);
    private final ScheduledExecutorService mockExecutorService = mock(ScheduledExecutorService.class);

    @SuppressWarnings("rawtypes")
    private final ScheduledFuture mockFuture = mock(ScheduledFuture.class);

    @Test
    void nullHealthCheckTriggersInstantiationFailure() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AsyncHealthCheckDecorator(null, mockExecutorService);
        });
    }

    @Test
    void nullExecutorServiceTriggersInstantiationFailure() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AsyncHealthCheckDecorator(mockHealthCheck, null);
        });
    }

    @Test
    void nonAsyncHealthCheckTriggersInstantiationFailure() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AsyncHealthCheckDecorator(mockHealthCheck, mockExecutorService);
        });
    }

    @Test
    void negativePeriodTriggersInstantiationFailure() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AsyncHealthCheckDecorator(new NegativePeriodAsyncHealthCheck(), mockExecutorService);
        });
    }

    @Test
    void zeroPeriodTriggersInstantiationFailure() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AsyncHealthCheckDecorator(new ZeroPeriodAsyncHealthCheck(), mockExecutorService);
        });
    }

    @Test
    void negativeInitialValueTriggersInstantiationFailure() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AsyncHealthCheckDecorator(new NegativeInitialDelayAsyncHealthCheck(), mockExecutorService);
        });
    }

    @Test
    void defaultAsyncHealthCheckTriggersSuccessfulInstantiationWithFixedRateAndHealthyState() throws Exception {
        HealthCheck asyncHealthCheck = new DefaultAsyncHealthCheck();
        AsyncHealthCheckDecorator asyncDecorator = new AsyncHealthCheckDecorator(asyncHealthCheck, mockExecutorService);

        verify(mockExecutorService, times(1)).scheduleAtFixedRate(any(Runnable.class), eq(0L),
                eq(1L), eq(TimeUnit.SECONDS));
        assertThat(asyncDecorator.getHealthCheck()).isEqualTo(asyncHealthCheck);
        assertThat(asyncDecorator.check().isHealthy()).isTrue();
    }

    @Test
    void fixedDelayAsyncHealthCheckTriggersSuccessfulInstantiationWithFixedDelay() throws Exception {
        HealthCheck asyncHealthCheck = new FixedDelayAsyncHealthCheck();
        AsyncHealthCheckDecorator asyncDecorator = new AsyncHealthCheckDecorator(asyncHealthCheck, mockExecutorService);

        verify(mockExecutorService, times(1)).scheduleWithFixedDelay(any(Runnable.class), eq(0L),
                eq(1L), eq(TimeUnit.SECONDS));
        assertThat(asyncDecorator.getHealthCheck()).isEqualTo(asyncHealthCheck);
    }

    @Test
    void unhealthyAsyncHealthCheckTriggersSuccessfulInstantiationWithUnhealthyState() throws Exception {
        HealthCheck asyncHealthCheck = new UnhealthyAsyncHealthCheck();
        AsyncHealthCheckDecorator asyncDecorator = new AsyncHealthCheckDecorator(asyncHealthCheck, mockExecutorService);

        assertThat(asyncDecorator.check().isHealthy()).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void tearDownTriggersCancellation() throws Exception {
        when(mockExecutorService.scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS))).
                thenReturn(mockFuture);
        when(mockFuture.cancel(true)).thenReturn(true);

        AsyncHealthCheckDecorator asyncDecorator = new AsyncHealthCheckDecorator(new DefaultAsyncHealthCheck(), mockExecutorService);
        asyncDecorator.tearDown();

        verify(mockExecutorService, times(1)).scheduleAtFixedRate(any(Runnable.class), eq(0L),
                eq(1L), eq(TimeUnit.SECONDS));
        verify(mockFuture, times(1)).cancel(eq(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    void afterFirstExecutionDecoratedHealthCheckResultIsProvided() throws Exception {
        HealthCheck.Result expectedResult = HealthCheck.Result.healthy("AsyncHealthCheckTest");
        when(mockExecutorService.scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS)))
                .thenReturn(mockFuture);

        AsyncHealthCheckDecorator asyncDecorator = new AsyncHealthCheckDecorator(new ConfigurableAsyncHealthCheck(expectedResult),
                mockExecutorService);
        HealthCheck.Result initialResult = asyncDecorator.check();

        ArgumentCaptor<Runnable> runnableCaptor = forClass(Runnable.class);
        verify(mockExecutorService, times(1)).scheduleAtFixedRate(runnableCaptor.capture(),
                eq(0L), eq(1L), eq(TimeUnit.SECONDS));
        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();
        HealthCheck.Result actualResult = asyncDecorator.check();

        assertThat(actualResult).isEqualTo(expectedResult);
        assertThat(actualResult).isNotEqualTo(initialResult);
    }

    @Test
    @SuppressWarnings("unchecked")
    void exceptionInDecoratedHealthCheckWontAffectAsyncDecorator() throws Exception {
        Exception exception = new Exception("TestException");
        when(mockExecutorService.scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS)))
                .thenReturn(mockFuture);

        AsyncHealthCheckDecorator asyncDecorator = new AsyncHealthCheckDecorator(new ConfigurableAsyncHealthCheck(exception),
                mockExecutorService);

        ArgumentCaptor<Runnable> runnableCaptor = forClass(Runnable.class);
        verify(mockExecutorService, times(1)).scheduleAtFixedRate(runnableCaptor.capture(),
                eq(0L), eq(1L), eq(TimeUnit.SECONDS));
        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();
        HealthCheck.Result result = asyncDecorator.check();

        assertThat(result.isHealthy()).isFalse();
        assertThat(result.getError()).isEqualTo(exception);
    }

    @Test
    void returnUnhealthyIfPreviousResultIsExpiredBasedOnTtl() throws Exception {
        HealthCheck healthCheck = new HealthyAsyncHealthCheckWithExpiredExplicitTtlInMilliseconds();
        AsyncHealthCheckDecorator asyncDecorator = new AsyncHealthCheckDecorator(healthCheck, mockExecutorService, FIXED_CLOCK);

        ArgumentCaptor<Runnable> runnableCaptor = forClass(Runnable.class);
        verify(mockExecutorService, times(1)).scheduleAtFixedRate(runnableCaptor.capture(),
                eq(0L), eq(1000L), eq(TimeUnit.MILLISECONDS));
        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        HealthCheck.Result result = asyncDecorator.check();

        assertThat(result).isEqualTo(EXPECTED_EXPIRED_RESULT);
    }

    @Test
    void returnUnhealthyIfPreviousResultIsExpiredBasedOnPeriod() throws Exception {
        HealthCheck healthCheck = new HealthyAsyncHealthCheckWithExpiredTtlInMillisecondsBasedOnPeriod();
        AsyncHealthCheckDecorator asyncDecorator = new AsyncHealthCheckDecorator(healthCheck, mockExecutorService, FIXED_CLOCK);

        ArgumentCaptor<Runnable> runnableCaptor = forClass(Runnable.class);
        verify(mockExecutorService, times(1)).scheduleAtFixedRate(runnableCaptor.capture(),
                eq(0L), eq(1000L), eq(TimeUnit.MILLISECONDS));
        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        HealthCheck.Result result = asyncDecorator.check();

        assertThat(result).isEqualTo(EXPECTED_EXPIRED_RESULT);
    }

    @Test
    void convertTtlToMillisecondsWhenCheckingExpiration() throws Exception {
        HealthCheck healthCheck = new HealthyAsyncHealthCheckWithExpiredExplicitTtlInSeconds();
        AsyncHealthCheckDecorator asyncDecorator = new AsyncHealthCheckDecorator(healthCheck, mockExecutorService, FIXED_CLOCK);

        ArgumentCaptor<Runnable> runnableCaptor = forClass(Runnable.class);
        verify(mockExecutorService, times(1)).scheduleAtFixedRate(runnableCaptor.capture(),
                eq(0L), eq(1L), eq(TimeUnit.SECONDS));
        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        HealthCheck.Result result = asyncDecorator.check();

        assertThat(result).isEqualTo(EXPECTED_EXPIRED_RESULT);
    }

    @Async(period = -1)
    private static class NegativePeriodAsyncHealthCheck implements HealthCheck {

        @Override
        public Result check() {
            return null;
        }
    }

    @Async(period = 0)
    private static class ZeroPeriodAsyncHealthCheck implements HealthCheck {

        @Override
        public Result check() {
            return null;
        }
    }

    @Async(period = 1, initialDelay = -1)
    private static class NegativeInitialDelayAsyncHealthCheck implements HealthCheck {

        @Override
        public Result check() {
            return null;
        }
    }

    @Async(period = 1)
    private static class DefaultAsyncHealthCheck implements HealthCheck {

        @Override
        public Result check() {
            return null;
        }
    }

    @Async(period = 1, scheduleType = Async.ScheduleType.FIXED_DELAY)
    private static class FixedDelayAsyncHealthCheck implements HealthCheck {

        @Override
        public Result check() {
            return null;
        }
    }

    @Async(period = 1, initialState = Async.InitialState.UNHEALTHY)
    private static class UnhealthyAsyncHealthCheck implements HealthCheck {

        @Override
        public Result check() {
            return null;
        }
    }

    @Async(period = 1, initialState = Async.InitialState.UNHEALTHY)
    private static class ConfigurableAsyncHealthCheck implements HealthCheck {
        private final Result result;
        private final Exception exception;

        ConfigurableAsyncHealthCheck(Result result) {
            this(result, null);
        }

        ConfigurableAsyncHealthCheck(Exception exception) {
            this(null, exception);
        }

        private ConfigurableAsyncHealthCheck(Result result, Exception exception) {
            this.result = result;
            this.exception = exception;
        }

        @Override
        public Result check() throws Exception {
            if (exception != null) {
                throw exception;
            }
            return result;
        }
    }

    @Async(period = 1000, initialState = Async.InitialState.UNHEALTHY, healthyTtl = 3000, unit = TimeUnit.MILLISECONDS)
    private static class HealthyAsyncHealthCheckWithExpiredExplicitTtlInMilliseconds implements HealthCheck {

        @Override
        public Result check() {
            return Result.builder().usingClock(clockWithFixedTime(CURRENT_TIME - 3001L)).healthy().build();
        }
    }

    @Async(period = 1, initialState = Async.InitialState.UNHEALTHY, healthyTtl = 5, unit = TimeUnit.SECONDS)
    private static class HealthyAsyncHealthCheckWithExpiredExplicitTtlInSeconds implements HealthCheck {

        @Override
        public Result check() {
            return Result.builder().usingClock(clockWithFixedTime(CURRENT_TIME - 5001L)).healthy().build();
        }
    }

    @Async(period = 1000, initialState = Async.InitialState.UNHEALTHY, unit = TimeUnit.MILLISECONDS)
    private static class HealthyAsyncHealthCheckWithExpiredTtlInMillisecondsBasedOnPeriod implements HealthCheck {

        @Override
        public Result check() {
            return Result.builder().usingClock(clockWithFixedTime(CURRENT_TIME - 2001L)).healthy().build();
        }
    }

    private static Clock clockWithFixedTime(final long time) {
        return new Clock() {
            @Override
            public long getTick() {
                return 0;
            }

            @Override
            public long getTime() {
                return time;
            }
        };
    }

}
