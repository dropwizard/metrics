package io.dropwizard.metrics5.jersey2;

import io.dropwizard.metrics5.Clock;

public class TestClock extends Clock {

    public long tick;

    @Override
    public long getTick() {
        return tick;
    }
}
