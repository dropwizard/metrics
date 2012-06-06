package com.example.metrics.spring;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Clock.CpuTimeClock;
import com.yammer.metrics.core.MetricsRegistry;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Field;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class AltClockTest {

    @Test
    public void testOverriddenClock() throws Throwable {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:alt-clock.xml");
    	MetricsRegistry registry = ctx.getBean(MetricsRegistry.class);

      	assertThat(getClockField().get(registry), instanceOf(CpuTimeClock.class));
    }

    protected Field getClockField() throws NoSuchFieldException {
        for (Field f : MetricsRegistry.class.getDeclaredFields()) {
            if (f.getType() == Clock.class) {
                f.setAccessible(true);
                return f;
            }
        }
        throw new NoSuchFieldException();
    }
}
