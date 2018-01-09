package io.dropwizard.metrics5.health.jvm;

import io.dropwizard.metrics5.health.HealthCheck;
import io.dropwizard.metrics5.jvm.ThreadDeadlockDetector;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThreadDeadlockHealthCheckTest {
    @Test
    public void isHealthyIfNoThreadsAreDeadlocked() {
        final ThreadDeadlockDetector detector = mock(ThreadDeadlockDetector.class);
        final ThreadDeadlockHealthCheck healthCheck = new ThreadDeadlockHealthCheck(detector);

        when(detector.getDeadlockedThreads()).thenReturn(Collections.emptySet());

        Assertions.assertThat(healthCheck.execute().isHealthy())
                .isTrue();
    }

    @Test
    public void isUnhealthyIfThreadsAreDeadlocked() {
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
    public void automaticallyUsesThePlatformThreadBeans() {
        final ThreadDeadlockHealthCheck healthCheck = new ThreadDeadlockHealthCheck();
        Assertions.assertThat(healthCheck.execute().isHealthy())
                .isTrue();
    }
}
