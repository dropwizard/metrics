package com.yammer.metrics.util.tests;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.util.DeathRattleExceptionHandler;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeathRattleExceptionHandlerTest {
    private final Counter counter = mock(Counter.class);
    private final DeathRattleExceptionHandler handler = new DeathRattleExceptionHandler(counter);

    @Test
    public void incrementsTheCounterWhenAnExceptionIsThrown() throws Exception {
        final Throwable e = new Throwable();

        handler.uncaughtException(Thread.currentThread(), e);
        
        verify(counter).inc();
    }
}
