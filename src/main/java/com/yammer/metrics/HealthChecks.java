package com.yammer.metrics;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.yammer.metrics.core.DeadlockHealthCheck;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheck.Result;

/**
 * A manager class for health checks.
 */
public class HealthChecks {
	private static final ConcurrentMap<String, HealthCheck> HEALTH_CHECKS = new ConcurrentHashMap<String, HealthCheck>();
	static {
		registerHealthCheck(new DeadlockHealthCheck());
	}

	private HealthChecks() { /* unused */ }

	/**
	 * Registers an application {@link HealthCheck} with a given name.
	 *
	 * @param healthCheck the {@link HealthCheck} instance
	 */
	public static void registerHealthCheck(HealthCheck healthCheck) {
		HEALTH_CHECKS.putIfAbsent(healthCheck.name(), healthCheck);
	}

	/**
	 * Returns {@code true} if any {@link HealthCheck}s have been registered,
	 * {@code false} otherwise.
	 *
	 * @return if any {@link HealthCheck}s have been registered
	 */
	public static boolean hasHealthChecks() {
		return !HEALTH_CHECKS.isEmpty();
	}

	/**
	 * Runs the registered health checks and returns a map of the results.
	 *
	 * @return a map of the health check results
	 */
	public static Map<String, Result> runHealthChecks() {
		final Map<String, Result> results = new TreeMap<String, Result>();
		for (Entry<String, HealthCheck> entry : HEALTH_CHECKS.entrySet()) {
			final Result result = entry.getValue().execute();
			results.put(entry.getKey(), result);
		}
		return results;
	}
}
