package io.dropwizard.metrics;

import static org.junit.Assert.*;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class LongAdderFactoryTest {

    @Test
    public void test() {
        LongAdder longAdder = LongAdderFactory.create();
        assertThat(longAdder, CoreMatchers.instanceOf(UnsafeLongAdderImpl.class));
        assertThat(longAdder, CoreMatchers.instanceOf(UnsafeStriped64.class));
    }

}
