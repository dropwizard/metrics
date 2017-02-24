package com.codahale.metrics.health;

import static com.codahale.metrics.health.HealthCheck.Result;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.annotation.Async;

/**
 * A registry for health checks.
 */
public class HealthCheckRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckRegistry.class);

    private final ConcurrentMap<String, HealthCheck> healthChecks;
    private final List<HealthCheckRegistryListener> listeners;
    private final ScheduledExecutorService asyncExecutorService;
    private final Object lock = new Object();

    /**
     * Creates a new {@link HealthCheckRegistry}.
     */
    public HealthCheckRegistry() {
        this(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Creates a new {@link HealthCheckRegistry}.
     *
     * @param asyncExecutorPoolSize core pool size for async health check executions
     */
    public HealthCheckRegistry(int asyncExecutorPoolSize) {
        this(createExecutorService(asyncExecutorPoolSize));
    }

    /**
     * Creates a new {@link HealthCheckRegistry}.
     *
     * @param asyncExecutorService executor service for async health check executions
     */
    public HealthCheckRegistry(ScheduledExecutorService asyncExecutorService) {
        this.healthChecks = new ConcurrentHashMap<String, HealthCheck>();
        this.listeners = new CopyOnWriteArrayList<HealthCheckRegistryListener>();
        this.asyncExecutorService = asyncExecutorService;
    }

    /**
     * Adds a {@link HealthCheckRegistryListener} to a collection of listeners that will be notified on health check
     * registration. Listeners will be notified in the order in which they are added. The listener will be notified of all
     * existing health checks when it first registers.
     *
     * @param listener listener to add
     */
    public void addListener(HealthCheckRegistryListener listener) {
        listeners.add(listener);
        for (Map.Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            listener.onHealthCheckAdded(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes a {@link HealthCheckRegistryListener} from this registry's collection of listeners.
     *
     * @param listener listener to remove
     */
    public void removeListener(HealthCheckRegistryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Registers an application {@link HealthCheck}.
     *
     * @param name        the name of the health check
     * @param healthCheck the {@link HealthCheck} instance
     */
    public void register(String name, HealthCheck healthCheck) {
        HealthCheck registered = null;
        synchronized (lock) {
            if (!healthChecks.containsKey(name)) {
                registered = healthCheck;
                if (healthCheck.getClass().isAnnotationPresent(Async.class)) {
                    registered = new AsyncHealthCheckDecorator(healthCheck, asyncExecutorService);
                }
                healthChecks.put(name, registered);
            }
        }
        if (registered != null) {
            onHealthCheckAdded(name, registered);
        }
    }

    /**
     * Unregisters the application {@link HealthCheck} with the given name.
     *
     * @param name the name of the {@link HealthCheck} instance
     */
    public void unregister(String name) {
        HealthCheck healthCheck = null;
        synchronized (lock) {
            healthCheck = healthChecks.remove(name);
            if (healthCheck instanceof AsyncHealthCheckDecorator) {
                ((AsyncHealthCheckDecorator) healthCheck).tearDown();
            }
        }
        if (healthCheck != null) {
            onHealthCheckRemoved(name, healthCheck);
        }
    }

    /**
     * Returns a set of the names of all registered health checks.
     *
     * @return the names of all registered health checks
     */
    public SortedSet<String> getNames() {
        return Collections.unmodifiableSortedSet(new TreeSet<String>(healthChecks.keySet()));
    }

    /**
     * Runs the health check with the given name.
     *
     * @param name the health check's name
     * @return the result of the health check
     * @throws NoSuchElementException if there is no health check with the given name
     */
    public HealthCheck.Result runHealthCheck(String name) throws NoSuchElementException {
        final HealthCheck healthCheck = healthChecks.get(name);
        if (healthCheck == null) {
            throw new NoSuchElementException("No health check named " + name + " exists");
        }
        return healthCheck.execute();
    }

    /**
     * Runs the registered health checks and returns a map of the results.
     *
     * @return a map of the health check results
     */
    public SortedMap<String, HealthCheck.Result> runHealthChecks() {
        final SortedMap<String, HealthCheck.Result> results = new TreeMap<String, HealthCheck.Result>();
        for (Map.Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            final Result result = entry.getValue().execute();
            results.put(entry.getKey(), result);
        }
        return Collections.unmodifiableSortedMap(results);
    }

    /**
     * Runs the registered health checks in parallel and returns a map of the results.
     *
     * @param executor object to launch and track health checks progress
     * @return a map of the health check results
     */
    public SortedMap<String, HealthCheck.Result> runHealthChecks(ExecutorService executor) {
        final Map<String, Future<HealthCheck.Result>> futures = new HashMap<String, Future<Result>>();
        for (final Map.Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            futures.put(entry.getKey(), executor.submit(new Callable<Result>() {
                @Override
                public Result call() throws Exception {
                    return entry.getValue().execute();
                }
            }));
        }

        final SortedMap<String, HealthCheck.Result> results = new TreeMap<String, HealthCheck.Result>();
        for (Map.Entry<String, Future<Result>> entry : futures.entrySet()) {
            try {
                results.put(entry.getKey(), entry.getValue().get());
            } catch (Exception e) {
                LOGGER.warn("Error executing health check {}", entry.getKey(), e);
                results.put(entry.getKey(), HealthCheck.Result.unhealthy(e));
            }
        }

        return Collections.unmodifiableSortedMap(results);
    }


    private void onHealthCheckAdded(String name, HealthCheck healthCheck) {
        for (HealthCheckRegistryListener listener : listeners) {
            listener.onHealthCheckAdded(name, healthCheck);
        }
    }

    private void onHealthCheckRemoved(String name, HealthCheck healthCheck) {
        for (HealthCheckRegistryListener listener : listeners) {
            listener.onHealthCheckRemoved(name, healthCheck);
        }
    }

    /**
     * Shuts down the scheduled executor for async health checks
     */
    public void shutdown() {
        asyncExecutorService.shutdown(); // Disable new health checks from being submitted
        try {
            // Give some time to the current healtch checks to finish gracefully
            if (!asyncExecutorService.awaitTermination(1, TimeUnit.SECONDS)) {
                asyncExecutorService.shutdownNow();
            }
        } catch (InterruptedException ie) {
            asyncExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static ScheduledExecutorService createExecutorService(int corePoolSize) {
        ScheduledExecutorService asyncExecutorService = Executors.newScheduledThreadPool(corePoolSize,
                new NamedThreadFactory("healthcheck-async-executor-"));
        try {
            Method method = asyncExecutorService.getClass().getMethod("setRemoveOnCancelPolicy", Boolean.TYPE);
            method.invoke(asyncExecutorService, true);
        } catch (NoSuchMethodException e) {
            logSetExecutorCancellationPolicyFailure(e);
        } catch (IllegalAccessException e) {
            logSetExecutorCancellationPolicyFailure(e);
        } catch (InvocationTargetException e) {
            logSetExecutorCancellationPolicyFailure(e);
        }
        return asyncExecutorService;
    }

    private static void logSetExecutorCancellationPolicyFailure(Exception e) {
        LOGGER.warn("Tried but failed to set executor cancellation policy to remove on cancel which has been introduced " +
                "in Java 7. This could result in a memory leak if many asynchronous health checks are registered and " +
                "removed because cancellation does not actually remove them from the executor.", e);
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
