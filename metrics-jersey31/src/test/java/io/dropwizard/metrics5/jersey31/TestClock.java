package io.dropwizard.metrics5.jersey31;

import io.dropwizard.metrics5.Clock;

public class TestClock extends Clock {

    public long tick;

    @Override
    public long getTick() {
        return tick;
    }
}
