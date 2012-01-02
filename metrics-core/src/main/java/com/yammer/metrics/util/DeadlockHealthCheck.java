package com.yammer.metrics.util;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.VirtualMachineMetrics;

import java.util.Set;

/**
 * A {@link com.yammer.metrics.core.HealthCheck} implementation which returns a list of deadlocked threads, if any.
 */
public class DeadlockHealthCheck extends HealthCheck {
    private final VirtualMachineMetrics vm;

    public DeadlockHealthCheck(VirtualMachineMetrics vm) {
        super("deadlocks");
        this.vm = vm;
    }

    @SuppressWarnings("UnusedDeclaration")
    public DeadlockHealthCheck() {
        this(VirtualMachineMetrics.INSTANCE);
    }

    @Override
    protected Result check() throws Exception {
        final Set<String> threads = vm.deadlockedThreads();
        if (threads.isEmpty()) {
            return Result.healthy();
        }

        final StringBuilder builder = new StringBuilder("Deadlocked threads detected:\n");
        for (String thread : threads) {
            builder.append(thread).append('\n');
        }
        return Result.unhealthy(builder.toString());
    }
}
