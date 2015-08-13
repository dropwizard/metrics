package com.codahale.metrics.health;

import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;

@Deprecated
public class HealthCheckRegistry {
	final io.dropwizard.metrics.health.HealthCheckRegistry reg;

	public HealthCheckRegistry(io.dropwizard.metrics.health.HealthCheckRegistry reg) {
		this.reg = reg;
	}

	public static HealthCheckRegistry of(io.dropwizard.metrics.health.HealthCheckRegistry reg) {
		return new HealthCheckRegistry(reg);
	}

	public void register(String name, HealthCheck healthCheck) {
		reg.register(name, healthCheck);
	}

	public void unregister(String name) {
		reg.unregister(name);
	}

	public SortedSet<String> getNames() {
		return reg.getNames();
	}

	public HealthCheck.Result runHealthCheck(String name) throws NoSuchElementException {
		return HealthCheck.Result.of(reg.runHealthCheck(name));
	}

	public SortedMap<String, HealthCheck.Result> runHealthChecks() {
		return HealthCheck.Result.of(reg.runHealthChecks());
	}

	public SortedMap<String, HealthCheck.Result> runHealthChecks(ExecutorService executor) {
		return HealthCheck.Result.of(reg.runHealthChecks(executor));
	}
}
