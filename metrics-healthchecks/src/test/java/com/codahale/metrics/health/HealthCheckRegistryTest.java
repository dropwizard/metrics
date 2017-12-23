package com.codahale.metrics.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.codahale.metrics.health.annotation.Async;

public class HealthCheckRegistryTest {
    private final ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
    private final HealthCheckRegistry registry = new HealthCheckRegistry(executorService);
    private final HealthCheckRegistryListener listener = mock(HealthCheckRegistryListener.class);

    private final HealthCheck hc1 = mock(HealthCheck.class);
    private final HealthCheck hc2 = mock(HealthCheck.class);

    private final HealthCheck.Result r1 = mock(HealthCheck.Result.class);
    private final HealthCheck.Result r2 = mock(HealthCheck.Result.class);

    private final HealthCheck.Result ar = mock(HealthCheck.Result.class);
    private final HealthCheck ahc = new TestAsyncHealthCheck(ar);

    @SuppressWarnings("rawtypes")
    private final ScheduledFuture af = mock(ScheduledFuture.class);

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        registry.addListener(listener);

        when(hc1.execute()).thenReturn(r1);
        when(hc2.execute()).thenReturn(r2);
        when(executorService.scheduleAtFixedRate(any(AsyncHealthCheckDecorator.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS)))
            .thenReturn(af);

        registry.register("hc1", hc1);
        registry.register("hc2", hc2);
        registry.register("ahc", ahc);
    }

    @Test
    public void asyncHealthCheckIsScheduledOnExecutor() {
        ArgumentCaptor<AsyncHealthCheckDecorator> decoratorCaptor = forClass(AsyncHealthCheckDecorator.class);
        verify(executorService).scheduleAtFixedRate(decoratorCaptor.capture(), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
        assertThat(decoratorCaptor.getValue().getHealthCheck()).isEqualTo(ahc);
    }

    @Test
    public void asyncHealthCheckIsCanceledOnRemove() {
        registry.unregister("ahc");

        verify(af).cancel(true);
    }

    @Test
    public void registeringHealthCheckTriggersNotification() {
        verify(listener).onHealthCheckAdded("hc1", hc1);
        verify(listener).onHealthCheckAdded("hc2", hc2);
        verify(listener).onHealthCheckAdded(eq("ahc"), any(AsyncHealthCheckDecorator.class));
    }

    @Test
    public void removingHealthCheckTriggersNotification() {
        registry.unregister("hc1");
        registry.unregister("hc2");
        registry.unregister("ahc");

        verify(listener).onHealthCheckRemoved("hc1", hc1);
        verify(listener).onHealthCheckRemoved("hc2", hc2);
        verify(listener).onHealthCheckRemoved(eq("ahc"), any(AsyncHealthCheckDecorator.class));
    }

    @Test
    public void addingListenerCatchesExistingHealthChecks() {
        HealthCheckRegistryListener listener = mock(HealthCheckRegistryListener.class);
        HealthCheckRegistry registry = new HealthCheckRegistry();
        registry.register("hc1", hc1);
        registry.register("hc2", hc2);
        registry.register("ahc", ahc);
        registry.addListener(listener);

        verify(listener).onHealthCheckAdded("hc1", hc1);
        verify(listener).onHealthCheckAdded("hc2", hc2);
        verify(listener).onHealthCheckAdded(eq("ahc"), any(AsyncHealthCheckDecorator.class));
    }

    @Test
    public void removedListenerDoesNotReceiveUpdates() {
        HealthCheckRegistryListener listener = mock(HealthCheckRegistryListener.class);
        HealthCheckRegistry registry = new HealthCheckRegistry();
        registry.addListener(listener);
        registry.register("hc1", hc1);
        registry.removeListener(listener);
        registry.register("hc2", hc2);

        verify(listener).onHealthCheckAdded("hc1", hc1);
    }

    @Test
    public void runsRegisteredHealthChecks() {
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks();

        assertThat(results).contains(entry("hc1", r1));
        assertThat(results).contains(entry("hc2", r2));
        assertThat(results).containsKey("ahc");
    }

    @Test
    public void runsRegisteredHealthChecksWithFilter() {
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks((name, healthCheck) -> "hc1".equals(name));

        assertThat(results).containsOnly(entry("hc1", r1));
    }

    @Test
    public void runsRegisteredHealthChecksWithNonMatchingFilter() {
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks((name, healthCheck) -> false);

        assertThat(results).isEmpty();
    }

    @Test
    public void runsRegisteredHealthChecksInParallel() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks(executor);

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(results).contains(entry("hc1", r1));
        assertThat(results).contains(entry("hc2", r2));
        assertThat(results).containsKey("ahc");
    }

    @Test
    public void runsRegisteredHealthChecksInParallelWithNonMatchingFilter() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks(executor, (name, healthCheck) -> false);

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(results).isEmpty();
    }

    @Test
    public void runsRegisteredHealthChecksInParallelWithFilter() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks(executor,
            (name, healthCheck) -> "hc2".equals(name));

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(results).containsOnly(entry("hc2", r2));
    }

    @Test
    public void removesRegisteredHealthChecks() {
        registry.unregister("hc1");

        final Map<String, HealthCheck.Result> results = registry.runHealthChecks();

        assertThat(results).doesNotContainKey("hc1");
        assertThat(results).containsKey("hc2");
        assertThat(results).containsKey("ahc");
    }

    @Test
    public void hasASetOfHealthCheckNames() {
        assertThat(registry.getNames()).containsOnly("hc1", "hc2", "ahc");
    }

    @Test
    public void runsHealthChecksByName() {
        assertThat(registry.runHealthCheck("hc1")).isEqualTo(r1);
    }

    @Test
    public void doesNotRunNonexistentHealthChecks()  {
        try {
            registry.runHealthCheck("what");
            failBecauseExceptionWasNotThrown(NoSuchElementException.class);
        } catch (NoSuchElementException e) {
            assertThat(e.getMessage())
                .isEqualTo("No health check named what exists");
        }

    }

    @Async(period = 10)
    private static class TestAsyncHealthCheck extends HealthCheck {
        private final Result result;

        TestAsyncHealthCheck(Result result) {
            this.result = result;
        }

        @Override
        protected Result check() {
            return result;
        }
    }
}
