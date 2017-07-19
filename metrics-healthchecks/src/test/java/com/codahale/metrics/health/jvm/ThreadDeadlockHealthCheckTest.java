package com.codahale.metrics.health.jvm;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.jvm.ThreadDeadlockDetector;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThreadDeadlockHealthCheckTest {
    @Test
    public void isHealthyIfNoThreadsAreDeadlocked() throws Exception {
        final ThreadDeadlockDetector detector = mock(ThreadDeadlockDetector.class);
        final ThreadDeadlockHealthCheck healthCheck = new ThreadDeadlockHealthCheck(detector);

        when(detector.getDeadlockedThreads()).thenReturn(Collections.<String>emptySet());

        assertThat(healthCheck.execute().isHealthy())
                .isTrue();
    }

    @Test
    public void isUnhealthyIfThreadsAreDeadlocked() throws Exception {
        final Set<String> threads = new TreeSet<>();
        threads.add("one");
        threads.add("two");

        final ThreadDeadlockDetector detector = mock(ThreadDeadlockDetector.class);
        final ThreadDeadlockHealthCheck healthCheck = new ThreadDeadlockHealthCheck(detector);

        when(detector.getDeadlockedThreads()).thenReturn(threads);

        final HealthCheck.Result result = healthCheck.execute();

        assertThat(result.isHealthy())
                .isFalse();

        assertThat(result.getMessage())
                .isEqualTo("[one, two]");
    }

    @Test
    public void automaticallyUsesThePlatformThreadBeans() throws Exception {
        final ThreadDeadlockHealthCheck healthCheck = new ThreadDeadlockHealthCheck();
        assertThat(healthCheck.execute().isHealthy())
                .isTrue();
    }
}
