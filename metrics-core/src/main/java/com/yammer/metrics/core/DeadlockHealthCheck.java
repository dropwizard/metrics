package com.yammer.metrics.core;

import java.util.Set;

/**
 * A {@link HealthCheck} implementation which returns a list of deadlocked threads, if any.
 */
public class DeadlockHealthCheck extends HealthCheck {
    private final VirtualMachineMetrics vm = VirtualMachineMetrics.INSTANCE;

    @Override
    public Result check() throws Exception {
        final Set<String> threads = vm.deadlockedThreads();
        if (!threads.isEmpty()) {
            final StringBuilder builder = new StringBuilder("Deadlocked threads detected:\n");
            for (String thread : threads) {
                builder.append(thread).append('\n');
            }
            return Result.unhealthy(builder.toString());
        }
        return Result.healthy();
    }

    @Override
    public String name() {
        return "deadlocks";
    }
}
