package com.codahale.metrics.health;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import com.codahale.metrics.health.HealthCheck.Result;
import com.codahale.metrics.health.HealthCheckRegistry.Listener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HealthCheckNotificationTest
{
    private static final String CHECK1 = "check1";
    private static final String CHECK2 = "check2";

    private final AtomicReference<Result> result1 = new AtomicReference<Result>(Result.healthy());
    private final AtomicReference<Result> result2 = new AtomicReference<Result>(Result.healthy());

    private final HealthCheckRegistry registry = new HealthCheckRegistry();

    private final HealthCheck check1 = new HealthCheck() {
        @Override
        protected Result check() throws Exception
        {
            return result1.get();
        }
    };
    private final HealthCheck check2 = new HealthCheck() {
        @Override
        protected Result check() throws Exception
        {
            return result2.get();
        }
    };

    private final HealthCheckRegistry.Listener listener = mock(Listener.class);
    private ExecutorService pool;

    @Before
    public void setUp() throws Exception
    {
        pool = Executors.newCachedThreadPool();

        registry.register(CHECK1, check1);
        registry.register(CHECK2, check2);

        registry.addListener(listener);
    }

    @After
    public void tearDown() throws Exception
    {
        pool.shutdown();
    }

    @Test
    public void testAllNotification() throws Exception
    {
        Result newResult1 = Result.unhealthy("test");
        Result newResult2 = Result.unhealthy(new Throwable());

        Result final2 = Result.healthy("woop");

        registry.runHealthChecks();
        result1.set(newResult1);
        result2.set(newResult2);
        registry.runHealthChecks(pool);
        result2.set(final2);
        registry.runHealthCheck(CHECK2);

        verify(listener).checkCompleted(CHECK1, Result.healthy());
        verify(listener).checkCompleted(CHECK2, Result.healthy());

        verify(listener).checkCompleted(CHECK1, newResult1);
        verify(listener).checkCompleted(CHECK2, newResult2);

        verify(listener).checkCompleted(CHECK2, final2);

        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testListenerThrowingOK() throws Exception
    {
        registry.addListener(new Listener() {
            @Override
            public void checkCompleted(String name, Result result)
            {
                throw new IllegalStateException();
            }
        });

        registry.runHealthChecks();
    }
}
