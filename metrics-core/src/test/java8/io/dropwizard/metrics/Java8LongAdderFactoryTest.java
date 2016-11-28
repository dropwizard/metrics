package io.dropwizard.metrics;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

import org.junit.Test;

public class Java8LongAdderFactoryTest {

    @Test
    public void test() {
        assumeThat(Runtime.class.getPackage().getImplementationVersion(), startsWith("1.8"));

        LongAdder longAdder = LongAdderFactory.create();
        assertThat(longAdder, instanceOf(Java8LongAdderImpl.class));
    }

}
