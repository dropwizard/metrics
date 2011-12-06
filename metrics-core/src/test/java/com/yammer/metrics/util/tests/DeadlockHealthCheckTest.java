package com.yammer.metrics.util.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.util.DeadlockHealthCheck;

public class DeadlockHealthCheckTest {
    private final VirtualMachineMetrics vm = mock(VirtualMachineMetrics.class);
    private final DeadlockHealthCheck healthCheck = new DeadlockHealthCheck(vm);

    @Test
    public void hasAName() throws Exception {
        assertThat(healthCheck.name(),
                equalTo(new MetricName(DeadlockHealthCheck.class, "deadlocks")));
                   //is("deadlocks"));
    }

    @Test
    public void returnsHealthyIfNoDeadlocks() throws Exception {
        when(vm.deadlockedThreads()).thenReturn(new HashSet<String>());

        assertThat(healthCheck.execute(),
                   is(HealthCheck.Result.healthy()));
    }

    @Test
    public void returnsUnhealthyIfDeadlocks() throws Exception {
        final Set<String> threads = new HashSet<String>();
        threads.add("thread1");
        threads.add("thread2");

        when(vm.deadlockedThreads()).thenReturn(threads);
        
        assertThat(healthCheck.execute(),
                   is(HealthCheck.Result.unhealthy("Deadlocked threads detected:\n" +
                                                           "thread1\n" +
                                                           "thread2\n")));
    }
}
