package io.dropwizard.metrics.jersey31;

import com.codahale.metrics.Clock;

public class TestClock extends Clock {

    public long tick;

    @Override
    public long getTick() {
        return tick;
    }
}
