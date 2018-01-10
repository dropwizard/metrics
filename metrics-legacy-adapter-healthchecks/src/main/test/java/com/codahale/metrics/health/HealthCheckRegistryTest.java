package com.codahale.metrics.health;

import org.junit.After;
import org.junit.Test;

import java.util.SortedMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class HealthCheckRegistryTest {

    public static class DbCheck extends HealthCheck {
        @Override
        protected Result check() throws Exception {
            return Result.unhealthy("DB is down");
        }
    }

    public static class DummyHealthCheck extends HealthCheck {
        @Override
        protected Result check() throws Exception {
            return Result.healthy();
        }
    }

    public static class ProviderApiCheck extends HealthCheck {
        @Override
        protected Result check() throws Exception {
            return Result.healthy("Provider API is up");
        }
    }

    private HealthCheckRegistry registry = new HealthCheckRegistry();

    @After
    public void tearDown() throws Exception {
        registry.shutdown();
    }

    @Test
    public void testCreate() {
        registry.register("test-hc", new DummyHealthCheck());
        assertThat(registry.getNames()).containsExactly("test-hc");
    }

    @Test
    public void testCreateWithCustomPool() {
        registry = new HealthCheckRegistry(4);
        registry.register("test-hc", new DummyHealthCheck());
        assertThat(registry.getNames()).containsExactly("test-hc");
    }

    @Test
    public void testCreateWithCustomExecutor() {
        registry = new HealthCheckRegistry(Executors.newSingleThreadScheduledExecutor());
        registry.register("test-hc", new DummyHealthCheck());
        assertThat(registry.getNames()).containsExactly("test-hc");
    }

    @Test
    public void testUnregisterHealthCheck() {
        registry.register("test-hc", new DummyHealthCheck());
        registry.register("provider-api-hc", new ProviderApiCheck());

        registry.unregister("test-hc");
        assertThat(registry.getNames()).containsExactly("provider-api-hc");
    }

    @Test
    public void testRunHealthChecks() {
        registry.register("test-hc", new DummyHealthCheck());
        registry.register("db-hc", new DbCheck());

        SortedMap<String, HealthCheck.Result> healthChecks = registry.runHealthChecks();
        assertThat(healthChecks).containsOnlyKeys("test-hc", "db-hc");
        assertThat(healthChecks.get("test-hc").isHealthy()).isTrue();
        assertThat(healthChecks.get("db-hc").isHealthy()).isFalse();
        assertThat(healthChecks.get("db-hc").getMessage()).isEqualTo("DB is down");
    }

    @Test
    public void testRunSpecificHealthCheck() {
        registry.register("test-hc", new DummyHealthCheck());
        registry.register("db-hc", new DbCheck());

        HealthCheck.Result healthCheck = registry.runHealthCheck("db-hc");
        assertThat(healthCheck.isHealthy()).isFalse();
        assertThat(healthCheck.getMessage()).isEqualTo("DB is down");
    }

    @Test
    public void testRunHealthCheckWithFilter() {
        registry.register("test-hc", new DummyHealthCheck());
        registry.register("db-hc", new DbCheck());
        registry.register("provider-api-hc", new ProviderApiCheck());

        SortedMap<String, HealthCheck.Result> healthChecks = registry.runHealthChecks(
                (name, healthCheck) -> !name.equals("test-hc"));
        assertThat(healthChecks).containsOnlyKeys("provider-api-hc", "db-hc");
        assertThat(healthChecks.get("provider-api-hc").isHealthy()).isTrue();
        assertThat(healthChecks.get("provider-api-hc").getMessage()).isEqualTo("Provider API is up");
        assertThat(healthChecks.get("db-hc").isHealthy()).isFalse();
        assertThat(healthChecks.get("db-hc").getMessage()).isEqualTo("DB is down");
    }

    @Test
    public void testRunHealthCheckWithoutFiltering() {
        registry.register("test-hc", new DummyHealthCheck());
        registry.register("db-hc", new DbCheck());
        registry.register("provider-api-hc", new ProviderApiCheck());

        assertThat(registry.runHealthChecks(HealthCheckFilter.ALL))
                .containsOnlyKeys("test-hc", "provider-api-hc", "db-hc");
    }

    @Test
    public void testAddListener() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        registry.addListener(new HealthCheckRegistryListener() {
            @Override
            public void onHealthCheckAdded(String name, HealthCheck healthCheck) {
                assertThat(name).isEqualTo("test-hc");
                latch.countDown();
            }

            @Override
            public void onHealthCheckRemoved(String name, HealthCheck healthCheck) {

            }
        });

        registry.register("test-hc", new DummyHealthCheck());

        latch.await(5, TimeUnit.SECONDS);
        assertThat(latch.getCount()).isEqualTo(0);
    }

    @Test
    public void testAddListenerRemoveCallback() throws Exception {
        registry.register("test-hc", new DummyHealthCheck());

        CountDownLatch latch = new CountDownLatch(1);
        registry.addListener(new HealthCheckRegistryListener() {
            @Override
            public void onHealthCheckAdded(String name, HealthCheck healthCheck) {
            }

            @Override
            public void onHealthCheckRemoved(String name, HealthCheck healthCheck) {
                assertThat(name).isEqualTo("test-hc");
                latch.countDown();
            }
        });

        registry.unregister("test-hc");

        latch.await(5, TimeUnit.SECONDS);
        assertThat(latch.getCount()).isEqualTo(0);
    }

    @Test
    public void testRemoveListener() throws Exception {
        CountDownLatch addLatch = new CountDownLatch(1);
        CountDownLatch removeLatch = new CountDownLatch(1);
        HealthCheckRegistryListener listener = new HealthCheckRegistryListener() {
            @Override
            public void onHealthCheckAdded(String name, HealthCheck healthCheck) {
                assertThat(name).isEqualTo("test-hc");
                addLatch.countDown();
            }

            @Override
            public void onHealthCheckRemoved(String name, HealthCheck healthCheck) {
                assertThat(name).isEqualTo("test-hc");
                removeLatch.countDown();
            }
        };
        registry.addListener(listener);
        registry.register("test-hc", new DummyHealthCheck());
        addLatch.await(5, TimeUnit.SECONDS);
        assertThat(addLatch.getCount()).isEqualTo(0);

        registry.removeListener(listener);

        registry.unregister("test-hc");
        removeLatch.await(100, TimeUnit.MILLISECONDS);
        assertThat(removeLatch.getCount()).isEqualTo(1);
    }
}
