package com.codahale.metrics.health;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HealthCheckRegistryTest {
    private final HealthCheckRegistry registry = new HealthCheckRegistry();
    private final HealthCheckRegistryListener listener = mock(HealthCheckRegistryListener.class);

    private final HealthCheck hc1 = mock(HealthCheck.class);
    private final HealthCheck hc2 = mock(HealthCheck.class);

    private final HealthCheck.Result r1 = mock(HealthCheck.Result.class);
    private final HealthCheck.Result r2 = mock(HealthCheck.Result.class);

    @Before
    public void setUp() throws Exception {
        registry.addListener(listener);

        when(hc1.execute()).thenReturn(r1);

        when(hc2.execute()).thenReturn(r2);

        registry.register("hc1", hc1);
        registry.register("hc2", hc2);
    }

    @Test
    public void registeringHealthCheckTriggersNotification() {
        verify(listener).onHealthCheckAdded("hc1", hc1);
        verify(listener).onHealthCheckAdded("hc2", hc2);
    }

    @Test
    public void removingHealthCheckTriggersNotification() {
        registry.unregister("hc1");
        registry.unregister("hc2");

        verify(listener).onHealthCheckRemoved("hc1");
        verify(listener).onHealthCheckRemoved("hc2");
    }

    @Test
    public void addingListenerCatchesExistingHealthChecks() {
        HealthCheckRegistryListener listener = mock(HealthCheckRegistryListener.class);
        HealthCheckRegistry registry = new HealthCheckRegistry();
        registry.register("hc1", hc1);
        registry.register("hc2", hc2);
        registry.addListener(listener);

        verify(listener).onHealthCheckAdded("hc1", hc1);
        verify(listener).onHealthCheckAdded("hc2", hc2);
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
    public void runsRegisteredHealthChecks() throws Exception {
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks();

        assertThat(results)
                .contains(entry("hc1", r1));

        assertThat(results)
                .contains(entry("hc2", r2));
    }

    @Test
    public void runsRegisteredHealthChecksInParallel() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks(executor);

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(results)
                .contains(entry("hc1", r1));

        assertThat(results)
                .contains(entry("hc2", r2));
    }

    @Test
    public void removesRegisteredHealthChecks() throws Exception {
        registry.unregister("hc1");

        final Map<String, HealthCheck.Result> results = registry.runHealthChecks();

        assertThat(results)
                .doesNotContainKey("hc1");

        assertThat(results)
                .containsKey("hc2");
    }

    @Test
    public void hasASetOfHealthCheckNames() throws Exception {
        assertThat(registry.getNames())
                .containsOnly("hc1", "hc2");
    }

    @Test
    public void runsHealthChecksByName() throws Exception {
        assertThat(registry.runHealthCheck("hc1"))
                .isEqualTo(r1);
    }

    @Test
    public void doesNotRunNonexistentHealthChecks() throws Exception {
        try {
            registry.runHealthCheck("what");
            failBecauseExceptionWasNotThrown(NoSuchElementException.class);
        } catch (NoSuchElementException e) {
            assertThat(e.getMessage())
                    .isEqualTo("No health check named what exists");
        }

    }
}
