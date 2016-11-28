package io.dropwizard.metrics;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

import org.junit.Test;

public class LongAdderFactoryTest {

    @Test
    public void test() {
        assumeThat(Runtime.class.getPackage().getImplementationVersion(), startsWith("1.7"));

        LongAdder longAdder = LongAdderFactory.create();
        assertThat(longAdder, instanceOf(UnsafeLongAdderImpl.class));
        assertThat(longAdder, instanceOf(UnsafeStriped64.class));
    }

}
